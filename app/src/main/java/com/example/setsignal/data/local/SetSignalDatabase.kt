package com.example.setsignal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.setsignal.data.local.entity.SessionExerciseEntity
import com.example.setsignal.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [WorkoutSessionEntity::class, SessionExerciseEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SetSignalDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        const val NAME = "setsignal.db"

        fun build(context: Context): SetSignalDatabase =
            Room.databaseBuilder(context, SetSignalDatabase::class.java, NAME).build()
    }
}
