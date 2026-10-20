package com.example.setsignal.domain.data

import com.example.setsignal.domain.model.Exercise
import com.example.setsignal.domain.model.ExerciseTarget
import com.example.setsignal.domain.model.Routine

/**
 * Local example routines. They exist to make the prototype usable out of the box and to exercise
 * the workout flow. They are configuration examples, not medical advice or a personal training plan.
 */
class RoutineCatalog(
    val routines: List<Routine> = EXAMPLE_ROUTINES,
) {
    fun find(id: String): Routine? = routines.firstOrNull { it.id == id }

    fun require(id: String): Routine =
        find(id) ?: error("Unknown routine id: $id")

    companion object {
        val EXAMPLE_ROUTINES: List<Routine> = listOf(
            Routine(
                id = "full-body",
                name = "Cuerpo completo",
                tagline = "Fuerza · 4 ejercicios",
                description = "Un circuito de fuerza con el propio peso. Descansa entre series y " +
                    "mantén un ritmo que puedas sostener.",
                recommendedRestSeconds = 75,
                exercises = listOf(
                    Exercise(
                        id = "full-body-squat",
                        name = "Sentadilla",
                        order = 0,
                        sets = 3,
                        target = ExerciseTarget.Repetitions(12),
                    ),
                    Exercise(
                        id = "full-body-pushup",
                        name = "Flexiones",
                        order = 1,
                        sets = 3,
                        target = ExerciseTarget.Repetitions(10),
                    ),
                    Exercise(
                        id = "full-body-lunge",
                        name = "Zancadas alternas",
                        order = 2,
                        sets = 3,
                        target = ExerciseTarget.Repetitions(10),
                    ),
                    Exercise(
                        id = "full-body-plank",
                        name = "Plancha",
                        order = 3,
                        sets = 3,
                        target = ExerciseTarget.Duration(30),
                    ),
                ),
            ),
            Routine(
                id = "intervals",
                name = "Intervalos",
                tagline = "Cardio · 3 bloques",
                description = "Bloques cortos de movimiento continuo con descansos breves. " +
                    "Elige tu propio ritmo y baja la intensidad si lo necesitas.",
                recommendedRestSeconds = 20,
                exercises = listOf(
                    Exercise(
                        id = "intervals-jacks",
                        name = "Jumping jacks",
                        order = 0,
                        sets = 4,
                        target = ExerciseTarget.Duration(40),
                    ),
                    Exercise(
                        id = "intervals-high-knees",
                        name = "Rodillas altas",
                        order = 1,
                        sets = 4,
                        target = ExerciseTarget.Duration(30),
                    ),
                    Exercise(
                        id = "intervals-march",
                        name = "Marcha en el sitio",
                        order = 2,
                        sets = 4,
                        target = ExerciseTarget.Duration(30),
                    ),
                ),
            ),
            Routine(
                id = "calisthenics",
                name = "Calistenia suave",
                tagline = "Movilidad · 4 ejercicios",
                description = "Una sesión accesible para empezar a moverse. Prioriza el control " +
                    "sobre la velocidad y ajusta el rango de movimiento.",
                recommendedRestSeconds = 45,
                exercises = listOf(
                    Exercise(
                        id = "calisthenics-wall-pushup",
                        name = "Flexiones en pared",
                        order = 0,
                        sets = 3,
                        target = ExerciseTarget.Repetitions(12),
                    ),
                    Exercise(
                        id = "calisthenics-chair-squat",
                        name = "Sentadilla a silla",
                        order = 1,
                        sets = 3,
                        target = ExerciseTarget.Repetitions(10),
                    ),
                    Exercise(
                        id = "calisthenics-glute-bridge",
                        name = "Puente de glúteo",
                        order = 2,
                        sets = 3,
                        target = ExerciseTarget.Repetitions(12),
                    ),
                    Exercise(
                        id = "calisthenics-bird-dog",
                        name = "Bird dog",
                        order = 3,
                        sets = 3,
                        target = ExerciseTarget.Repetitions(8),
                    ),
                ),
            ),
        )
    }
}
