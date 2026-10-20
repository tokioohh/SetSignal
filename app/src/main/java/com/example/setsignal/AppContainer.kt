package com.example.setsignal

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.setsignal.data.datastore.DataStoreSettingsRepository
import com.example.setsignal.data.local.SetSignalDatabase
import com.example.setsignal.data.repository.RoomSessionRepository
import com.example.setsignal.domain.data.RoutineCatalog
import com.example.setsignal.domain.repository.SessionRepository
import com.example.setsignal.domain.repository.SettingsRepository
import com.example.setsignal.platform.HapticFeedback
import com.example.setsignal.platform.SystemTimeSource
import com.example.setsignal.platform.TimeSource
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency container. It is intentionally small: the MVP has one database, one preference
 * store and a single haptics adapter, so a DI framework would add indirection without value.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: SetSignalDatabase = SetSignalDatabase.build(appContext)

    val sessionRepository: SessionRepository = RoomSessionRepository(database.sessionDao())

    private val dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(
        PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(appContext.filesDir, "setsignal_settings.preferences_pb") },
        ),
    )

    val routineCatalog: RoutineCatalog = RoutineCatalog()

    val haptics: HapticFeedback = HapticFeedback(appContext)

    val timeSource: TimeSource = SystemTimeSource
}
