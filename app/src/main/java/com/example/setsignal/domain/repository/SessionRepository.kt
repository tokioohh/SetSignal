package com.example.setsignal.domain.repository

import com.example.setsignal.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSessions(): Flow<List<WorkoutSession>>

    fun observeLatest(): Flow<WorkoutSession?>

    fun observeCompletedCount(): Flow<Int>

    suspend fun getSession(id: String): WorkoutSession?

    /** Persists a finished or cancelled session exactly once. */
    suspend fun saveSession(session: WorkoutSession)

    suspend fun clearHistory()
}
