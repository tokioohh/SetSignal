package com.example.setsignal.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setsignal.domain.model.WorkoutSession
import com.example.setsignal.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

sealed interface SummaryUiState {
    data object Loading : SummaryUiState

    data object NotFound : SummaryUiState

    data class Content(val session: WorkoutSession) : SummaryUiState
}

class SummaryViewModel(
    sessionId: String,
    sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<SummaryUiState> = flow {
        val session = runCatching { sessionRepository.getSession(sessionId) }.getOrNull()
        emit(if (session == null) SummaryUiState.NotFound else SummaryUiState.Content(session))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SummaryUiState.Loading,
    )
}
