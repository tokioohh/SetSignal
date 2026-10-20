package com.example.setsignal.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.example.setsignal.data.local.entity.SessionExerciseEntity
import com.example.setsignal.data.local.entity.WorkoutSessionEntity

data class SessionWithExercises(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val exercises: List<SessionExerciseEntity>,
)
