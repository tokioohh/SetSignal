package com.example.setsignal.domain.model

/**
 * The target for one set. Repetitions and timed holds are kept explicitly separate so the UI
 * never has to guess what a number means.
 */
sealed interface ExerciseTarget {
    data class Repetitions(val count: Int) : ExerciseTarget

    data class Duration(val seconds: Int) : ExerciseTarget

    /** Short value used inside the workout ring, for example "12" or "30". */
    val displayValue: String
        get() = when (this) {
            is Repetitions -> count.toString()
            is Duration -> seconds.toString()
        }

    /** Short unit shown under the value, for example "REPS" or "SEG". */
    val displayUnit: String
        get() = when (this) {
            is Repetitions -> "REPS"
            is Duration -> "SEG"
        }

    /** Complete target label used in lists and history, for example "3 × 12". */
    fun label(repetitions: Int): String = when (this) {
        is Repetitions -> count.toString()
        is Duration -> "$seconds s"
    }.let { "$repetitions × $it" }
}

data class Exercise(
    val id: String,
    val name: String,
    val order: Int,
    val sets: Int,
    val target: ExerciseTarget,
) {
    init {
        require(id.isNotBlank()) { "Exercise id must not be blank" }
        require(name.isNotBlank()) { "Exercise name must not be blank" }
        require(sets > 0) { "An exercise needs at least one set" }
        when (target) {
            is ExerciseTarget.Repetitions -> require(target.count > 0)
            is ExerciseTarget.Duration -> require(target.seconds > 0)
        }
    }
}

data class Routine(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val recommendedRestSeconds: Int,
    val exercises: List<Exercise>,
) {
    init {
        require(id.isNotBlank()) { "Routine id must not be blank" }
        require(name.isNotBlank()) { "Routine name must not be blank" }
        require(exercises.isNotEmpty()) { "A routine needs at least one exercise" }
        require(recommendedRestSeconds >= 0)
    }

    val totalSets: Int = exercises.sumOf { it.sets }
}
