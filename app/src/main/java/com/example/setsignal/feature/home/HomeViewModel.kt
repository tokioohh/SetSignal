package com.example.setsignal.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setsignal.domain.model.WorkoutSession
import com.example.setsignal.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val isLoading: Boolean = true,
    val lastSession: WorkoutSession? = null,
    val completedSessions: Int = 0,
)

class HomeViewModel(
    sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        sessionRepository.observeLatest(),
        sessionRepository.observeCompletedCount(),
    ) { latest, completed ->
        HomeUiState(
            isLoading = false,
            lastSession = latest,
            completedSessions = completed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}
