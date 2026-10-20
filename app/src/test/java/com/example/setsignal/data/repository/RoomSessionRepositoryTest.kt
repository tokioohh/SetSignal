package com.example.setsignal.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.setsignal.data.local.SetSignalDatabase
import com.example.setsignal.domain.model.CompletedExercise
import com.example.setsignal.domain.model.SessionStatus
import com.example.setsignal.domain.model.WorkoutSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomSessionRepositoryTest {

    private lateinit var database: SetSignalDatabase
    private lateinit var repository: RoomSessionRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SetSignalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomSessionRepository(database.sessionDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `saved session round trips every field and exercise order`() = runTest {
        val session = sampleSession(
            id = "session-1",
            routineName = "Fuerza total",
            startedAt = 1_000L,
            status = SessionStatus.COMPLETED,
            exercises = listOf(
                exercise("press", "Press banca", order = 1, planned = 3, completed = 2),
                exercise("sentadilla", "Sentadilla", order = 0, planned = 4, completed = 4),
            ),
        )

        repository.saveSession(session)

        val stored = repository.getSession("session-1")
        assertEquals(session.copy(exercises = session.exercises.sortedBy { it.order }), stored)
        assertEquals(listOf("Sentadilla", "Press banca"), stored!!.exercises.map { it.name })
    }

    @Test
    fun `history is newest first and exposes completed count`() = runTest {
        repository.saveSession(
            sampleSession(
                id = "older",
                routineName = "Movilidad",
                startedAt = 1_000L,
                status = SessionStatus.COMPLETED,
            ),
        )
        repository.saveSession(
            sampleSession(
                id = "newer",
                routineName = "Intervalos",
                startedAt = 2_000L,
                status = SessionStatus.CANCELLED,
            ),
        )

        val sessions = repository.observeSessions().first()

        assertEquals(listOf("newer", "older"), sessions.map { it.id })
        assertEquals(SessionStatus.CANCELLED, sessions[0].status)
        assertEquals(SessionStatus.COMPLETED, sessions[1].status)
        assertEquals(1, repository.observeCompletedCount().first())
        assertEquals("newer", repository.observeLatest().first()?.id)
    }

    @Test
    fun `clear history removes sessions and exercises`() = runTest {
        repository.saveSession(sampleSession(id = "one", startedAt = 1_000L))
        repository.saveSession(sampleSession(id = "two", startedAt = 2_000L))

        repository.clearHistory()

        assertTrue(repository.observeSessions().first().isEmpty())
        assertEquals(0, repository.observeCompletedCount().first())
        assertNull(repository.observeLatest().first())
        assertNull(repository.getSession("one"))
    }

    private fun sampleSession(
        id: String,
        routineName: String = "Fuerza total",
        startedAt: Long,
        status: SessionStatus = SessionStatus.COMPLETED,
        exercises: List<CompletedExercise> = listOf(
            exercise("sentadilla", "Sentadilla", order = 0, planned = 3, completed = 3),
        ),
    ) = WorkoutSession(
        id = id,
        routineId = "full-body",
        routineName = routineName,
        startedAtEpochMillis = startedAt,
        finishedAtEpochMillis = startedAt + 60_000L,
        durationSeconds = 60,
        plannedExercises = 2,
        completedExercises = 1,
        plannedSets = 6,
        completedSets = 4,
        status = status,
        restSeconds = 45,
        exercises = exercises,
    )

    private fun exercise(
        id: String,
        name: String,
        order: Int,
        planned: Int,
        completed: Int,
    ) = CompletedExercise(
        exerciseId = id,
        name = name,
        order = order,
        plannedSets = planned,
        completedSets = completed,
        targetLabel = "$planned × 10",
    )
}
