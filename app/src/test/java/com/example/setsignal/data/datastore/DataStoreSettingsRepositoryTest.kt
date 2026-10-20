package com.example.setsignal.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.setsignal.domain.model.UserSettings
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DataStoreSettingsRepositoryTest {

    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        tempDirectory = Files.createTempDirectory("setsignal-settings").toFile()
    }

    @After
    fun tearDown() {
        tempDirectory.deleteRecursively()
    }

    @Test
    fun `defaults are used when no preferences exist`() = runTest {
        val scope = newScope()
        try {
            val repository = repository(scope, settingsFile())

            assertEquals(
                UserSettings(
                    defaultRestSeconds = 60,
                    additionalRestSeconds = 15,
                    hapticsEnabled = true,
                ),
                repository.settings.first(),
            )
        } finally {
            cancelAndJoin(scope)
        }
    }

    @Test
    fun `values are clamped persisted and read back by a new store`() = runTest {
        val file = settingsFile()
        val firstScope = newScope()
        try {
            val repository = repository(firstScope, file)
            repository.setDefaultRestSeconds(1)
            repository.setAdditionalRestSeconds(999)
            repository.setHapticsEnabled(false)

            val saved = repository.settings.first()
            assertEquals(15, saved.defaultRestSeconds)
            assertEquals(60, saved.additionalRestSeconds)
            assertEquals(false, saved.hapticsEnabled)
        } finally {
            cancelAndJoin(firstScope)
        }

        val secondScope = newScope()
        try {
            val reopened = repository(secondScope, file)
            assertEquals(
                UserSettings(
                    defaultRestSeconds = 15,
                    additionalRestSeconds = 60,
                    hapticsEnabled = false,
                ),
                reopened.settings.first(),
            )
        } finally {
            cancelAndJoin(secondScope)
        }
    }

    @Test
    fun `valid values are stored without modification`() = runTest {
        val scope = newScope()
        try {
            val repository = repository(scope, settingsFile())
            repository.setDefaultRestSeconds(90)
            repository.setAdditionalRestSeconds(20)

            val saved = repository.settings.first()
            assertEquals(90, saved.defaultRestSeconds)
            assertEquals(20, saved.additionalRestSeconds)
            assertEquals(true, saved.hapticsEnabled)
        } finally {
            cancelAndJoin(scope)
        }
    }

    private fun repository(scope: CoroutineScope, file: File) = DataStoreSettingsRepository(
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        ),
    )

    private fun settingsFile() = File(tempDirectory, "settings.preferences_pb")

    private fun newScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private suspend fun cancelAndJoin(scope: CoroutineScope) {
        scope.cancel()
        scope.coroutineContext[Job]?.join()
    }
}
