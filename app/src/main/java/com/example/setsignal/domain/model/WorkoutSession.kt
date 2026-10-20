package com.example.setsignal.domain.model

enum class SessionStatus {
    COMPLETED,
    CANCELLED,
}

/** A finished exercise inside a saved session. It stores what actually happened, not the plan. */
data class CompletedExercise(
    val exerciseId: String,
    val name: String,
    val order: Int,
    val plannedSets: Int,
    val completedSets: Int,
    val targetLabel: String,
)

/**
 * A persisted workout. Active/paused sessions deliberately never reach this model: only a completed
 * or cancelled session is saved, which keeps the history free of duplicate or half-written entries.
 */
data class WorkoutSession(
    val id: String,
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
) {
    val setProgress: Float
        get() = if (plannedSets == 0) 0f else completedSets.toFloat() / plannedSets

    val wasCompleted: Boolean
        get() = status == SessionStatus.COMPLETED
}
