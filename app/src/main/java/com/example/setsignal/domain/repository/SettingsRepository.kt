package com.example.setsignal.domain.repository

import com.example.setsignal.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setDefaultRestSeconds(seconds: Int)

    suspend fun setAdditionalRestSeconds(seconds: Int)

    suspend fun setHapticsEnabled(enabled: Boolean)
}
