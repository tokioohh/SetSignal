package com.example.setsignal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val routineName: String,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val durationSeconds: Int,
    val plannedExercises: Int,
    val completedExercises: Int,
    val plannedSets: Int,
    val completedSets: Int,
    /** SessionStatus.name */
    val status: String,
    val restSeconds: Int,
)

@Entity(
    tableName = "session_exercises",
    primaryKeys = ["sessionId", "exerciseId"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class SessionExerciseEntity(
    val sessionId: String,
    val exerciseId: String,
    val name: String,
    val position: Int,
    val plannedSets: Int,
    val completedSets: Int,
    val targetLabel: String,
)
