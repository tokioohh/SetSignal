package com.example.setsignal.domain.model

data class UserSettings(
    val defaultRestSeconds: Int = DEFAULT_REST_SECONDS,
    val additionalRestSeconds: Int = DEFAULT_ADDITIONAL_REST_SECONDS,
    val hapticsEnabled: Boolean = true,
) {
    companion object {
        const val DEFAULT_REST_SECONDS = 60
        const val DEFAULT_ADDITIONAL_REST_SECONDS = 15
        const val MIN_REST_SECONDS = 15
        const val MAX_REST_SECONDS = 300
        const val REST_STEP_SECONDS = 15
        const val MIN_ADDITIONAL_REST_SECONDS = 5
        const val MAX_ADDITIONAL_REST_SECONDS = 60
        const val ADDITIONAL_REST_STEP_SECONDS = 5
    }
}
