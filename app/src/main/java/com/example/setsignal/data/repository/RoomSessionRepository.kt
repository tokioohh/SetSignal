package com.example.setsignal.data.repository

import com.example.setsignal.data.local.SessionDao
import com.example.setsignal.data.local.SessionWithExercises
import com.example.setsignal.data.local.entity.SessionExerciseEntity
import com.example.setsignal.data.local.entity.WorkoutSessionEntity
import com.example.setsignal.domain.model.CompletedExercise
import com.example.setsignal.domain.model.SessionStatus
import com.example.setsignal.domain.model.WorkoutSession
import com.example.setsignal.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSessionRepository(
    private val dao: SessionDao,
) : SessionRepository {

    override fun observeSessions(): Flow<List<WorkoutSession>> =
        dao.observeAll().map { sessions -> sessions.map(::toDomain) }

    override fun observeLatest(): Flow<WorkoutSession?> =
        dao.observeLatest().map { it?.let(::toDomain) }

    override fun observeCompletedCount(): Flow<Int> = dao.observeCompletedCount()

    override suspend fun getSession(id: String): WorkoutSession? =
        dao.getById(id)?.let(::toDomain)

    override suspend fun saveSession(session: WorkoutSession) {
        dao.insertSessionWithExercises(
            session = session.toEntity(),
            exercises = session.exercises.map { it.toEntity(session.id) },
        )
    }

    override suspend fun clearHistory() {
        dao.deleteAll()
    }
}

private fun toDomain(value: SessionWithExercises): WorkoutSession {
    val session = value.session
    return WorkoutSession(
        id = session.id,
        routineId = session.routineId,
        routineName = session.routineName,
        startedAtEpochMillis = session.startedAtEpochMillis,
        finishedAtEpochMillis = session.finishedAtEpochMillis,
        durationSeconds = session.durationSeconds,
        plannedExercises = session.plannedExercises,
        completedExercises = session.completedExercises,
        plannedSets = session.plannedSets,
        completedSets = session.completedSets,
        status = runCatching { SessionStatus.valueOf(session.status) }
            .getOrDefault(SessionStatus.CANCELLED),
        restSeconds = session.restSeconds,
        exercises = value.exercises
            .sortedBy { it.position }
            .map {
                CompletedExercise(
                    exerciseId = it.exerciseId,
                    name = it.name,
                    order = it.position,
                    plannedSets = it.plannedSets,
                    completedSets = it.completedSets,
                    targetLabel = it.targetLabel,
                )
            },
    )
}

private fun WorkoutSession.toEntity(): WorkoutSessionEntity =
    WorkoutSessionEntity(
        id = id,
        routineId = routineId,
        routineName = routineName,
        startedAtEpochMillis = startedAtEpochMillis,
        finishedAtEpochMillis = finishedAtEpochMillis,
        durationSeconds = durationSeconds,
        plannedExercises = plannedExercises,
        completedExercises = completedExercises,
        plannedSets = plannedSets,
        completedSets = completedSets,
        status = status.name,
        restSeconds = restSeconds,
    )

private fun CompletedExercise.toEntity(sessionId: String): SessionExerciseEntity =
    SessionExerciseEntity(
        sessionId = sessionId,
        exerciseId = exerciseId,
        name = name,
        position = order,
        plannedSets = plannedSets,
        completedSets = completedSets,
        targetLabel = targetLabel,
    )
