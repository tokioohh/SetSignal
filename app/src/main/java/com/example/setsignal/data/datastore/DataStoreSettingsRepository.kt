package com.example.setsignal.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.setsignal.domain.model.UserSettings
import com.example.setsignal.domain.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            UserSettings(
                defaultRestSeconds = (preferences[Keys.DefaultRest] ?: UserSettings.DEFAULT_REST_SECONDS)
                    .coerceIn(UserSettings.MIN_REST_SECONDS, UserSettings.MAX_REST_SECONDS),
                additionalRestSeconds = (preferences[Keys.AdditionalRest]
                    ?: UserSettings.DEFAULT_ADDITIONAL_REST_SECONDS)
                    .coerceIn(
                        UserSettings.MIN_ADDITIONAL_REST_SECONDS,
                        UserSettings.MAX_ADDITIONAL_REST_SECONDS,
                    ),
                hapticsEnabled = preferences[Keys.Haptics] ?: true,
            )
        }

    override suspend fun setDefaultRestSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.DefaultRest] = seconds.coerceIn(
                UserSettings.MIN_REST_SECONDS,
                UserSettings.MAX_REST_SECONDS,
            )
        }
    }

    override suspend fun setAdditionalRestSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.AdditionalRest] = seconds.coerceIn(
                UserSettings.MIN_ADDITIONAL_REST_SECONDS,
                UserSettings.MAX_ADDITIONAL_REST_SECONDS,
            )
        }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[Keys.Haptics] = enabled }
    }

    private object Keys {
        val DefaultRest = intPreferencesKey("default_rest_seconds")
        val AdditionalRest = intPreferencesKey("additional_rest_seconds")
        val Haptics = booleanPreferencesKey("haptics_enabled")
    }
}
