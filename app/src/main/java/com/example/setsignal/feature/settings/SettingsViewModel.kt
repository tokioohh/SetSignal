package com.example.setsignal.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setsignal.domain.model.UserSettings
import com.example.setsignal.domain.repository.SessionRepository
import com.example.setsignal.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings(),
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { SettingsUiState(isLoading = false, settings = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun changeDefaultRest(delta: Int) {
        val current = uiState.value.settings.defaultRestSeconds
        val next = (current + delta).coerceIn(
            UserSettings.MIN_REST_SECONDS,
            UserSettings.MAX_REST_SECONDS,
        )
        viewModelScope.launch { settingsRepository.setDefaultRestSeconds(next) }
    }

    fun changeAdditionalRest(delta: Int) {
        val current = uiState.value.settings.additionalRestSeconds
        val next = (current + delta).coerceIn(
            UserSettings.MIN_ADDITIONAL_REST_SECONDS,
            UserSettings.MAX_ADDITIONAL_REST_SECONDS,
        )
        viewModelScope.launch { settingsRepository.setAdditionalRestSeconds(next) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch { sessionRepository.clearHistory() }
    }
}
