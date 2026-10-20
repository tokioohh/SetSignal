package com.example.setsignal.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setsignal.domain.data.RoutineCatalog
import com.example.setsignal.domain.model.SessionStatus
import com.example.setsignal.domain.model.UserSettings
import com.example.setsignal.domain.repository.SessionRepository
import com.example.setsignal.domain.repository.SettingsRepository
import com.example.setsignal.domain.workout.WorkoutEngine
import com.example.setsignal.domain.workout.WorkoutPhase
import com.example.setsignal.domain.workout.WorkoutSnapshot
import com.example.setsignal.domain.workout.toSession
import com.example.setsignal.platform.HapticFeedback
import com.example.setsignal.platform.TimeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class WorkoutUiState(
    val active: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: Boolean = false,
    val savedSessionId: String? = null,
    val routineName: String = "",
    val phase: WorkoutPhase = WorkoutPhase.EXERCISE,
    val isPaused: Boolean = false,
    val exerciseName: String = "",
    val targetValue: String = "",
    val targetUnit: String = "",
    val setNumber: Int = 1,
    val setCount: Int = 1,
    val exerciseIndex: Int = 0,
    val exerciseCount: Int = 0,
    val completedSets: Int = 0,
    val totalSets: Int = 0,
    val completedExercises: Int = 0,
    val elapsedSeconds: Int = 0,
    val restRemainingSeconds: Int = 0,
    val restTotalSeconds: Int = 0,
    val restProgress: Float = 0f,
    val nextExerciseName: String? = null,
    val nextSetNumber: Int? = null,
    val nextSetCount: Int? = null,
    val nextTargetValue: String? = null,
    val nextTargetUnit: String? = null,
    val sessionProgress: Float = 0f,
    val canCompleteSet: Boolean = false,
    val isFinalSet: Boolean = false,
    val additionalRestSeconds: Int = 15,
) {
    val isResting: Boolean
        get() = phase == WorkoutPhase.REST || phase == WorkoutPhase.REST_COMPLETE

    val isRestComplete: Boolean
        get() = phase == WorkoutPhase.REST_COMPLETE
}

/**
 * Owns the live workout for the whole activity. The engine is driven by monotonic timestamps, so a
 * recomposition or a trip to the background cannot make the countdown drift.
 */
class WorkoutViewModel(
    private val routineCatalog: RoutineCatalog,
    private val sessionRepository: SessionRepository,
    settingsRepository: SettingsRepository,
    private val haptics: HapticFeedback,
    private val timeSource: TimeSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var engine: WorkoutEngine? = null
    private var tickerJob: Job? = null
    private var saveInProgress = false
    private var pendingSessionId: String? = null
    private var pendingSession: com.example.setsignal.domain.model.WorkoutSession? = null

    private var hapticsEnabled = true
    private var additionalRestSeconds = UserSettings.DEFAULT_ADDITIONAL_REST_SECONDS

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                hapticsEnabled = settings.hapticsEnabled
                additionalRestSeconds = settings.additionalRestSeconds
                _uiState.update { it.copy(additionalRestSeconds = settings.additionalRestSeconds) }
            }
        }
    }

    fun start(routineId: String, restSeconds: Int) {
        if (_uiState.value.active || saveInProgress) return
        val routine = routineCatalog.find(routineId) ?: return

        val nowElapsed = timeSource.elapsedRealtimeMillis()
        engine = WorkoutEngine(
            routine = routine,
            restSeconds = restSeconds.coerceIn(0, 600),
            startedAtEpochMillis = timeSource.epochMillis(),
            startedAtElapsedMillis = nowElapsed,
        )
        pendingSession = null
        pendingSessionId = null
        saveInProgress = false
        publish(nowElapsed)
        startTicker()
    }

    fun completeSet() {
        val activeEngine = engine ?: return
        val now = timeSource.elapsedRealtimeMillis()
        if (!activeEngine.completeSet(now, timeSource.epochMillis())) return
        if (hapticsEnabled) haptics.setCompleted()
        publish(now)
    }

    fun togglePause() {
        val activeEngine = engine ?: return
        val now = timeSource.elapsedRealtimeMillis()
        if (activeEngine.isPaused) {
            activeEngine.resume(now)
        } else {
            activeEngine.pause(now)
        }
        publish(now)
    }

    fun addRestTime() {
        val activeEngine = engine ?: return
        val now = timeSource.elapsedRealtimeMillis()
        if (activeEngine.addRestSeconds(additionalRestSeconds, now) && hapticsEnabled) {
            haptics.tick()
        }
        publish(now)
    }

    fun skipRest() {
        val activeEngine = engine ?: return
        if (!activeEngine.skipRest()) return
        if (hapticsEnabled) haptics.tick()
        publish(timeSource.elapsedRealtimeMillis())
    }

    fun cancelSession() {
        val activeEngine = engine ?: return
        if (activeEngine.isTerminal) return
        val now = timeSource.elapsedRealtimeMillis()
        activeEngine.cancel(now, timeSource.epochMillis())
        publish(now)
    }

    fun retrySave() {
        val session = pendingSession ?: return
        saveSession(session)
    }

    /** Called by the app after navigating away from a newly saved session. */
    fun consumeFinishedSession() {
        tickerJob?.cancel()
        tickerJob = null
        engine = null
        pendingSession = null
        pendingSessionId = null
        saveInProgress = false
        _uiState.value = WorkoutUiState()
    }

    /** Last-resort escape hatch when local storage cannot be written. */
    fun discardUnsavedSession() {
        consumeFinishedSession()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val activeEngine = engine ?: break
                val now = timeSource.elapsedRealtimeMillis()
                val restJustFinished = activeEngine.tick(now)
                if (restJustFinished && hapticsEnabled) {
                    haptics.restFinished()
                }
                publish(now)
                if (activeEngine.isTerminal) break
                delay(if (activeEngine.isPaused) 500L else 200L)
            }
        }
    }

    private fun publish(nowElapsedMillis: Long) {
        val activeEngine = engine ?: return
        val snapshot = activeEngine.snapshot(nowElapsedMillis)
        val current = _uiState.value

        _uiState.value = current.copy(
            active = true,
            routineName = snapshot.routineName,
            phase = snapshot.phase,
            isPaused = snapshot.isPaused,
            exerciseName = snapshot.exerciseName,
            targetValue = snapshot.exerciseTarget.displayValue,
            targetUnit = snapshot.exerciseTarget.displayUnit,
            setNumber = snapshot.setNumber,
            setCount = snapshot.setCount,
            exerciseIndex = snapshot.exerciseIndex,
            exerciseCount = snapshot.exerciseCount,
            completedSets = snapshot.completedSets,
            totalSets = snapshot.totalSets,
            completedExercises = snapshot.completedExercises,
            elapsedSeconds = snapshot.elapsedSeconds,
            restRemainingSeconds = snapshot.restRemainingSeconds,
            restTotalSeconds = (snapshot.restTotalMillis / 1000L).toInt(),
            restProgress = snapshot.restProgress,
            nextExerciseName = snapshot.nextExerciseName,
            nextSetNumber = snapshot.nextSetNumber,
            nextSetCount = snapshot.nextSetCount,
            nextTargetValue = snapshot.nextTarget?.displayValue,
            nextTargetUnit = snapshot.nextTarget?.displayUnit,
            sessionProgress = snapshot.sessionProgress,
            canCompleteSet = snapshot.canCompleteSet,
            isFinalSet = snapshot.isFinalSet,
        )

        when (snapshot.phase) {
            WorkoutPhase.FINISHED -> finishAndSave(SessionStatus.COMPLETED)
            WorkoutPhase.CANCELLED -> finishAndSave(SessionStatus.CANCELLED)
            else -> Unit
        }
    }

    private fun finishAndSave(status: SessionStatus) {
        if (saveInProgress || pendingSessionId != null) return

        val session = pendingSession ?: run {
            val activeEngine = engine ?: return
            activeEngine.result(status).toSession()
        }
        pendingSession = session
        saveSession(session)
    }

    private fun saveSession(session: com.example.setsignal.domain.model.WorkoutSession) {
        if (saveInProgress || pendingSessionId != null) return
        saveInProgress = true
        _uiState.update { it.copy(isSaving = true, saveError = false, active = true) }

        viewModelScope.launch {
            runCatching { sessionRepository.saveSession(session) }
                .onSuccess {
                    pendingSessionId = session.id
                    _uiState.update {
                        it.copy(isSaving = false, savedSessionId = session.id, saveError = false)
                    }
                }
                .onFailure {
                    saveInProgress = false
                    _uiState.update { it.copy(isSaving = false, saveError = true) }
                }
        }
    }
}
