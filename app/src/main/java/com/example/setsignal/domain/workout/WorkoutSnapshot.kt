package com.example.setsignal.domain.workout

import com.example.setsignal.domain.model.ExerciseTarget

/** Immutable view of the workout at one instant. All values are derived from the engine. */
data class WorkoutSnapshot(
    val phase: WorkoutPhase,
    val isPaused: Boolean,
    val routineName: String,
    val exerciseName: String,
    val exerciseTarget: ExerciseTarget,
    val setNumber: Int,
    val setCount: Int,
    val exerciseIndex: Int,
    val exerciseCount: Int,
    val completedSets: Int,
    val totalSets: Int,
    val completedExercises: Int,
    val elapsedSeconds: Int,
    val restRemainingMillis: Long,
    val restTotalMillis: Long,
    val nextExerciseName: String?,
    val nextSetNumber: Int?,
    val nextSetCount: Int?,
    val nextTarget: ExerciseTarget?,
    val sessionProgress: Float,
    val canCompleteSet: Boolean,
    val isFinalSet: Boolean,
) {
    val restRemainingSeconds: Int
        get() = if (restRemainingMillis <= 0L) 0 else ((restRemainingMillis + 999L) / 1000L).toInt()

    /** 1 means a full rest remains, 0 means it has elapsed. */
    val restProgress: Float
        get() = when {
            phase == WorkoutPhase.REST_COMPLETE -> 0f
            restTotalMillis <= 0L -> 0f
            else -> (restRemainingMillis.toFloat() / restTotalMillis.toFloat()).coerceIn(0f, 1f)
        }

    val isResting: Boolean
        get() = phase == WorkoutPhase.REST || phase == WorkoutPhase.REST_COMPLETE
}
