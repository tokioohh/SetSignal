package com.example.setsignal.domain.workout

import com.example.setsignal.domain.model.CompletedExercise
import com.example.setsignal.domain.model.Exercise
import com.example.setsignal.domain.model.Routine
import com.example.setsignal.domain.model.SessionStatus

/**
 * Pure workout state machine. Time is always supplied by the caller as a monotonic clock reading, so
 * the countdown stays accurate across recompositions, lifecycle changes and screen-off periods.
 *
 * Rules:
 * - Completing a set advances to the next set, or the next exercise, after the configured rest.
 * - Completing the final set of the final exercise finishes the session without a trailing rest.
 * - Only a completed or cancelled session can produce a result, so unfinished attempts are not
 *   written to history.
 * - Elapsed duration excludes paused time. Rest time counts as session time.
 * - A session can be paused during an exercise or a rest. The rest screen can always be skipped.
 */
class WorkoutEngine(
    val routine: Routine,
    val restSeconds: Int,
    val startedAtEpochMillis: Long,
    startedAtElapsedMillis: Long,
) {
    var phase: WorkoutPhase = WorkoutPhase.EXERCISE
        private set

    var isPaused: Boolean = false
        private set

    var currentExerciseIndex: Int = 0
        private set

    private val completedSetsByExercise = IntArray(routine.exercises.size)

    private var pendingExerciseIndex = 0
    private var pendingSetIndex = 0

    private var restTotalMillis = 0L
    private var restEndsAtElapsedMillis = 0L
    private var restRemainingAtPauseMillis = 0L

    private var elapsedAccumulatedMillis = 0L
    private var elapsedRunningSinceMillis = startedAtElapsedMillis
    private var finishedAtEpochMillis: Long? = null

    init {
        require(restSeconds >= 0) { "Rest cannot be negative" }
    }

    val currentExercise: Exercise
        get() = routine.exercises[currentExerciseIndex]

    val completedSetCount: Int
        get() = completedSetsByExercise.sum()

    val completedExerciseCount: Int
        get() = routine.exercises.indices.count { index ->
            completedSetsByExercise[index] == routine.exercises[index].sets
        }

    val isTerminal: Boolean
        get() = phase == WorkoutPhase.FINISHED || phase == WorkoutPhase.CANCELLED

    /** Marks the current set as done and starts a rest if one is configured. */
    fun completeSet(nowElapsedMillis: Long, nowEpochMillis: Long): Boolean {
        if (phase != WorkoutPhase.EXERCISE || isPaused) return false

        val exercise = currentExercise
        val completed = completedSetsByExercise[currentExerciseIndex]
        if (completed >= exercise.sets) return false

        completedSetsByExercise[currentExerciseIndex] = completed + 1

        val exerciseDone = completed + 1 == exercise.sets
        if (exerciseDone && currentExerciseIndex == routine.exercises.lastIndex) {
            finish(nowElapsedMillis, nowEpochMillis)
            return true
        }

        pendingExerciseIndex = if (exerciseDone) currentExerciseIndex + 1 else currentExerciseIndex
        pendingSetIndex = if (exerciseDone) 0 else completed + 1
        startRestOrAdvance(nowElapsedMillis)
        return true
    }

    /** Freezes both the elapsed clock and a running rest countdown. */
    fun pause(nowElapsedMillis: Long): Boolean {
        if (isPaused || phase == WorkoutPhase.FINISHED || phase == WorkoutPhase.CANCELLED ||
            phase == WorkoutPhase.REST_COMPLETE
        ) {
            return false
        }

        accumulateElapsed(nowElapsedMillis)
        if (phase == WorkoutPhase.REST) {
            restRemainingAtPauseMillis =
                (restEndsAtElapsedMillis - nowElapsedMillis).coerceAtLeast(0L)
        }
        isPaused = true
        return true
    }

    fun resume(nowElapsedMillis: Long): Boolean {
        if (!isPaused) return false

        isPaused = false
        elapsedRunningSinceMillis = nowElapsedMillis
        if (phase == WorkoutPhase.REST) {
            restEndsAtElapsedMillis = nowElapsedMillis + restRemainingAtPauseMillis
        }
        return true
    }

    /**
     * Advances the rest countdown to "complete" once its deadline is reached. Returns true only on
     * the transition, so the caller can trigger exactly one haptic event.
     */
    fun tick(nowElapsedMillis: Long): Boolean {
        if (phase == WorkoutPhase.REST && !isPaused &&
            nowElapsedMillis >= restEndsAtElapsedMillis
        ) {
            restRemainingAtPauseMillis = 0L
            phase = WorkoutPhase.REST_COMPLETE
            return true
        }
        return false
    }

    /** Adds time to the current rest. From REST_COMPLETE it starts a new short countdown. */
    fun addRestSeconds(seconds: Int, nowElapsedMillis: Long): Boolean {
        if (seconds <= 0) return false
        val addedMillis = seconds * 1000L

        when (phase) {
            WorkoutPhase.REST -> {
                restTotalMillis += addedMillis
                if (isPaused) {
                    restRemainingAtPauseMillis += addedMillis
                } else {
                    restEndsAtElapsedMillis += addedMillis
                }
                return true
            }

            WorkoutPhase.REST_COMPLETE -> {
                phase = WorkoutPhase.REST
                restTotalMillis = addedMillis
                restEndsAtElapsedMillis = nowElapsedMillis + addedMillis
                restRemainingAtPauseMillis = addedMillis
                return true
            }

            else -> return false
        }
    }

    /** Leaves the rest screen and moves to the next set. Paused state is respected. */
    fun skipRest(): Boolean {
        if (phase != WorkoutPhase.REST && phase != WorkoutPhase.REST_COMPLETE) return false
        applyPendingAdvance()
        return true
    }

    fun cancel(nowElapsedMillis: Long, nowEpochMillis: Long): Boolean {
        if (isTerminal) return false
        accumulateElapsed(nowElapsedMillis)
        finishedAtEpochMillis = nowEpochMillis
        phase = WorkoutPhase.CANCELLED
        isPaused = false
        return true
    }

    fun snapshot(nowElapsedMillis: Long): WorkoutSnapshot {
        val exercise = currentExercise
        val completedForExercise = completedSetsByExercise[currentExerciseIndex]

        val nextExerciseName: String?
        val nextSetNumber: Int?
        val nextSetCount: Int?
        val nextTarget: com.example.setsignal.domain.model.ExerciseTarget?

        if (phase == WorkoutPhase.REST || phase == WorkoutPhase.REST_COMPLETE) {
            val next = routine.exercises[pendingExerciseIndex]
            nextExerciseName = next.name
            nextSetNumber = pendingSetIndex + 1
            nextSetCount = next.sets
            nextTarget = next.target
        } else {
            val nextIndex = when {
                completedForExercise + 1 < exercise.sets -> currentExerciseIndex
                currentExerciseIndex < routine.exercises.lastIndex -> currentExerciseIndex + 1
                else -> null
            }
            if (nextIndex == null) {
                nextExerciseName = null
                nextSetNumber = null
                nextSetCount = null
                nextTarget = null
            } else {
                val next = routine.exercises[nextIndex]
                nextExerciseName = next.name
                nextSetNumber = if (nextIndex == currentExerciseIndex) completedForExercise + 2 else 1
                nextSetCount = next.sets
                nextTarget = next.target
            }
        }

        return WorkoutSnapshot(
            phase = phase,
            isPaused = isPaused,
            routineName = routine.name,
            exerciseName = exercise.name,
            exerciseTarget = exercise.target,
            setNumber = (completedForExercise + 1).coerceAtMost(exercise.sets),
            setCount = exercise.sets,
            exerciseIndex = currentExerciseIndex,
            exerciseCount = routine.exercises.size,
            completedSets = completedSetCount,
            totalSets = routine.totalSets,
            completedExercises = completedExerciseCount,
            elapsedSeconds = (elapsedMillis(nowElapsedMillis) / 1000L).toInt(),
            restRemainingMillis = restRemainingMillis(nowElapsedMillis),
            restTotalMillis = restTotalMillis,
            nextExerciseName = nextExerciseName,
            nextSetNumber = nextSetNumber,
            nextSetCount = nextSetCount,
            nextTarget = nextTarget,
            sessionProgress = if (routine.totalSets == 0) {
                0f
            } else {
                completedSetCount.toFloat() / routine.totalSets.toFloat()
            },
            canCompleteSet = phase == WorkoutPhase.EXERCISE && !isPaused,
            isFinalSet = phase == WorkoutPhase.EXERCISE &&
                currentExerciseIndex == routine.exercises.lastIndex &&
                completedForExercise + 1 == exercise.sets,
        )
    }

    fun result(status: SessionStatus): WorkoutResult {
        require(isTerminal) { "Only a finished or cancelled session can produce a result" }
        if (status == SessionStatus.COMPLETED) {
            require(phase == WorkoutPhase.FINISHED) { "Session has not finished" }
        }
        if (status == SessionStatus.CANCELLED) {
            require(phase == WorkoutPhase.CANCELLED) { "Session was not cancelled" }
        }

        val exercises = routine.exercises.mapIndexed { index, exercise ->
            CompletedExercise(
                exerciseId = exercise.id,
                name = exercise.name,
                order = exercise.order,
                plannedSets = exercise.sets,
                completedSets = completedSetsByExercise[index],
                targetLabel = exercise.target.label(exercise.sets),
            )
        }

        return WorkoutResult(
            routineId = routine.id,
            routineName = routine.name,
            startedAtEpochMillis = startedAtEpochMillis,
            finishedAtEpochMillis = requireNotNull(finishedAtEpochMillis),
            durationSeconds = (elapsedAccumulatedMillis / 1000L).toInt(),
            plannedExercises = routine.exercises.size,
            completedExercises = completedExerciseCount,
            plannedSets = routine.totalSets,
            completedSets = completedSetCount,
            status = status,
            restSeconds = restSeconds,
            exercises = exercises,
        )
    }

    private fun startRestOrAdvance(nowElapsedMillis: Long) {
        if (restSeconds <= 0) {
            applyPendingAdvance()
            return
        }
        phase = WorkoutPhase.REST
        restTotalMillis = restSeconds * 1000L
        restEndsAtElapsedMillis = nowElapsedMillis + restTotalMillis
        restRemainingAtPauseMillis = restTotalMillis
        isPaused = false
    }

    private fun applyPendingAdvance() {
        currentExerciseIndex = pendingExerciseIndex
        phase = WorkoutPhase.EXERCISE
        restTotalMillis = 0L
        restEndsAtElapsedMillis = 0L
        restRemainingAtPauseMillis = 0L
    }

    private fun finish(nowElapsedMillis: Long, nowEpochMillis: Long) {
        accumulateElapsed(nowElapsedMillis)
        finishedAtEpochMillis = nowEpochMillis
        phase = WorkoutPhase.FINISHED
        restTotalMillis = 0L
        restEndsAtElapsedMillis = 0L
        restRemainingAtPauseMillis = 0L
        isPaused = false
    }

    private fun restRemainingMillis(nowElapsedMillis: Long): Long = when (phase) {
        WorkoutPhase.REST -> if (isPaused) {
            restRemainingAtPauseMillis.coerceAtLeast(0L)
        } else {
            (restEndsAtElapsedMillis - nowElapsedMillis).coerceAtLeast(0L)
        }

        WorkoutPhase.REST_COMPLETE -> 0L
        else -> 0L
    }

    private fun elapsedMillis(nowElapsedMillis: Long): Long {
        val running = if (!isPaused && !isTerminal) {
            (nowElapsedMillis - elapsedRunningSinceMillis).coerceAtLeast(0L)
        } else {
            0L
        }
        return elapsedAccumulatedMillis + running
    }

    private fun accumulateElapsed(nowElapsedMillis: Long) {
        if (!isPaused) {
            elapsedAccumulatedMillis +=
                (nowElapsedMillis - elapsedRunningSinceMillis).coerceAtLeast(0L)
        }
        elapsedRunningSinceMillis = nowElapsedMillis
    }
}
