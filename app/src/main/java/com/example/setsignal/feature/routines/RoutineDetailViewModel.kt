package com.example.setsignal.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setsignal.domain.data.RoutineCatalog
import com.example.setsignal.domain.model.Routine
import com.example.setsignal.domain.model.UserSettings
import com.example.setsignal.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineDetailViewModel(
    routineId: String,
    routineCatalog: RoutineCatalog,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val routine: Routine? = routineCatalog.find(routineId)

    private val _restSeconds = MutableStateFlow(UserSettings.DEFAULT_REST_SECONDS)
    val restSeconds: StateFlow<Int> = _restSeconds.asStateFlow()

    private var userAdjustedRest = false

    init {
        viewModelScope.launch {
            val preferred = settingsRepository.settings.first().defaultRestSeconds
            if (!userAdjustedRest) _restSeconds.value = preferred
        }
    }

    fun changeRest(deltaSeconds: Int) {
        userAdjustedRest = true
        _restSeconds.update { current ->
            (current + deltaSeconds).coerceIn(0, MAX_ROUTINE_REST_SECONDS)
        }
    }

    fun useRecommendedRest() {
        val recommended = routine?.recommendedRestSeconds ?: return
        userAdjustedRest = true
        _restSeconds.value = recommended
    }

    companion object {
        const val MAX_ROUTINE_REST_SECONDS = 300
        const val REST_STEP_SECONDS = 15
    }
}
