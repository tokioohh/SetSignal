package com.example.setsignal.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setsignal.domain.model.WorkoutSession
import com.example.setsignal.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<WorkoutSession> = emptyList(),
)

class HistoryViewModel(
    sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = sessionRepository.observeSessions()
        .map { sessions -> HistoryUiState(isLoading = false, sessions = sessions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState(),
        )
}
