package com.example.setsignal.domain.workout

enum class WorkoutPhase {
    /** Performing the current set. */
    EXERCISE,

    /** Rest countdown is running or paused. */
    REST,

    /** The countdown reached zero and is waiting for the user to continue. */
    REST_COMPLETE,

    /** All planned sets were completed. */
    FINISHED,

    /** The user ended the session early. */
    CANCELLED,
}
