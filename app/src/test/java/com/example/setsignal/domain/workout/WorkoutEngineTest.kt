package com.example.setsignal.domain.workout

import com.example.setsignal.domain.model.Exercise
import com.example.setsignal.domain.model.ExerciseTarget
import com.example.setsignal.domain.model.Routine
import com.example.setsignal.domain.model.SessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class WorkoutEngineTest {

    private val twoByTwo = Routine(
        id = "test",
        name = "Test routine",
        tagline = "test",
        description = "test",
        recommendedRestSeconds = 30,
        exercises = listOf(
            Exercise("ex-1", "First", 0, 2, ExerciseTarget.Repetitions(10)),
            Exercise("ex-2", "Second", 1, 2, ExerciseTarget.Duration(30)),
        ),
    )

    @Test
    fun `completing a set starts a timestamp based rest`() {
        val engine = WorkoutEngine(twoByTwo, restSeconds = 30, startedAtEpochMillis = 1_000L, startedAtElapsedMillis = 0L)

        assertTrue(engine.completeSet(nowElapsedMillis = 10_000L, nowEpochMillis = 11_000L))

        assertEquals(WorkoutPhase.REST, engine.phase)
        assertEquals(1, engine.completedSetCount)
        assertEquals(30_000L, engine.snapshot(10_000L).restRemainingMillis)
        assertEquals(20_000L, engine.snapshot(20_000L).restRemainingMillis)
    }

    @Test
    fun `rest completion fires only on the transition`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.completeSet(0L, 0L)

        assertFalse(engine.tick(29_999L))
        assertEquals(WorkoutPhase.REST, engine.phase)
        assertTrue(engine.tick(30_000L))
        assertEquals(WorkoutPhase.REST_COMPLETE, engine.phase)
        assertEquals(0L, engine.snapshot(30_000L).restRemainingMillis)
        assertFalse(engine.tick(60_000L))
    }

    @Test
    fun `skip rest advances to the next set of the same exercise`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.completeSet(5_000L, 0L)
        engine.tick(35_000L)

        assertTrue(engine.skipRest())

        val snapshot = engine.snapshot(36_000L)
        assertEquals(WorkoutPhase.EXERCISE, snapshot.phase)
        assertEquals(0, snapshot.exerciseIndex)
        assertEquals(2, snapshot.setNumber)
        assertEquals(2, snapshot.setCount)
    }

    @Test
    fun `skip rest moves to the first set of the next exercise`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.completeSet(1_000L, 0L)
        engine.skipRest()
        engine.completeSet(2_000L, 0L)
        engine.skipRest()

        val snapshot = engine.snapshot(3_000L)
        assertEquals(1, snapshot.exerciseIndex)
        assertEquals("Second", snapshot.exerciseName)
        assertEquals(1, snapshot.setNumber)
        assertEquals(1, snapshot.completedExercises)
    }

    @Test
    fun `the final set finishes without a trailing rest`() {
        val engine = WorkoutEngine(twoByTwo, 30, 100L, 0L)
        engine.completeSet(1_000L, 1_100L)
        engine.skipRest()
        engine.completeSet(2_000L, 2_100L)
        engine.skipRest()
        engine.completeSet(3_000L, 3_100L)
        engine.skipRest()
        engine.completeSet(5_000L, 5_100L)

        assertTrue(engine.isTerminal)
        assertEquals(WorkoutPhase.FINISHED, engine.phase)

        val result = engine.result(SessionStatus.COMPLETED)
        assertEquals(4, result.completedSets)
        assertEquals(4, result.plannedSets)
        assertEquals(2, result.completedExercises)
        assertEquals(5, result.durationSeconds)
        assertEquals(SessionStatus.COMPLETED, result.status)
    }

    @Test
    fun `rest seconds can be zero`() {
        val engine = WorkoutEngine(twoByTwo, 0, 0L, 0L)
        engine.completeSet(4_000L, 0L)

        val snapshot = engine.snapshot(4_000L)
        assertEquals(WorkoutPhase.EXERCISE, snapshot.phase)
        assertEquals(2, snapshot.setNumber)
        assertEquals(0L, snapshot.restTotalMillis)
    }

    @Test
    fun `pause freezes elapsed and rest time`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.completeSet(5_000L, 0L)

        assertTrue(engine.pause(10_000L))
        assertEquals(10, engine.snapshot(10_000L).elapsedSeconds)
        assertEquals(25_000L, engine.snapshot(10_000L).restRemainingMillis)
        assertEquals(25_000L, engine.snapshot(90_000L).restRemainingMillis)

        assertTrue(engine.resume(90_000L))
        assertEquals(25_000L, engine.snapshot(90_000L).restRemainingMillis)
        assertEquals(10, engine.snapshot(90_000L).elapsedSeconds)
        assertFalse(engine.tick(114_999L))
        assertTrue(engine.tick(115_000L))
    }

    @Test
    fun `a paused set cannot be completed`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.pause(1_000L)

        assertFalse(engine.completeSet(2_000L, 2_000L))
        assertEquals(0, engine.completedSetCount)
        assertEquals(WorkoutPhase.EXERCISE, engine.phase)
    }

    @Test
    fun `add rest time extends a running countdown`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.completeSet(0L, 0L)

        assertTrue(engine.addRestSeconds(15, 5_000L))
        assertEquals(40_000L, engine.snapshot(5_000L).restRemainingMillis)
        assertEquals(10_000L, engine.snapshot(35_000L).restRemainingMillis)
    }

    @Test
    fun `add rest time after completion starts a new short rest`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.completeSet(0L, 0L)
        engine.tick(30_000L)

        assertTrue(engine.addRestSeconds(15, 31_000L))
        assertEquals(WorkoutPhase.REST, engine.phase)
        assertEquals(15_000L, engine.snapshot(31_000L).restRemainingMillis)
        assertEquals(15_000L, engine.snapshot(31_000L).restTotalMillis)
    }

    @Test
    fun `cancel stores completed work and elapsed time`() {
        val engine = WorkoutEngine(twoByTwo, 30, 500L, 0L)
        engine.completeSet(5_000L, 5_500L)
        engine.skipRest()
        assertEquals(true, engine.cancel(20_000L, 20_500L))

        val result = engine.result(SessionStatus.CANCELLED)
        assertEquals(1, result.completedSets)
        assertEquals(0, result.completedExercises)
        assertEquals(20, result.durationSeconds)
        assertEquals(SessionStatus.CANCELLED, result.status)
        assertEquals(500L, result.startedAtEpochMillis)
        assertEquals(20_500L, result.finishedAtEpochMillis)
    }

    @Test
    fun `result requires a terminal phase and matching status`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        try {
            engine.result(SessionStatus.COMPLETED)
            fail("Expected an exception for an active session")
        } catch (expected: IllegalArgumentException) {
            assertNotNull(expected.message)
        }

        engine.cancel(1_000L, 1_000L)
        try {
            engine.result(SessionStatus.COMPLETED)
            fail("Expected an exception for a mismatched status")
        } catch (expected: IllegalArgumentException) {
            assertNotNull(expected.message)
        }
    }

    @Test
    fun `snapshot describes the next set during rest`() {
        val engine = WorkoutEngine(twoByTwo, 30, 0L, 0L)
        engine.completeSet(0L, 0L)

        val snapshot = engine.snapshot(1_000L)
        assertEquals("First", snapshot.nextExerciseName)
        assertEquals(2, snapshot.nextSetNumber)
        assertEquals(2, snapshot.nextSetCount)
        assertEquals(2, snapshot.setNumber)
        assertEquals(0.25f, snapshot.sessionProgress)
        assertTrue(snapshot.isResting)
    }

    @Test
    fun `final snapshot has no next set`() {
        val single = twoByTwo.copy(
            exercises = listOf(Exercise("only", "Only", 0, 1, ExerciseTarget.Repetitions(5))),
        )
        val engine = WorkoutEngine(single, 0, 0L, 0L)
        engine.completeSet(1_000L, 1_000L)

        val snapshot = engine.snapshot(1_000L)
        assertNull(snapshot.nextExerciseName)
        assertNull(snapshot.nextSetNumber)
        assertEquals(1f, snapshot.sessionProgress)
    }
}
