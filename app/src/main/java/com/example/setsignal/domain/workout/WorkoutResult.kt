package com.example.setsignal.domain.workout

import com.example.setsignal.domain.model.CompletedExercise
import com.example.setsignal.domain.model.SessionStatus

data class WorkoutResult(
    val routineId: String,
    val routineName: String,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val durationSeconds: Int,
    val plannedExercises: Int,
    val completedExercises: Int,
    val plannedSets: Int,
    val completedSets: Int,
    val status: SessionStatus,
    val restSeconds: Int,
    val exercises: List<CompletedExercise>,
)
