package com.example.setsignal.domain.workout

import com.example.setsignal.domain.model.WorkoutSession
import java.util.UUID

fun WorkoutResult.toSession(id: String = UUID.randomUUID().toString()): WorkoutSession =
    WorkoutSession(
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
        status = status,
        restSeconds = restSeconds,
        exercises = exercises,
    )
