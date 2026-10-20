package com.example.setsignal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.setsignal.data.local.entity.SessionExerciseEntity
import com.example.setsignal.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SessionWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY startedAtEpochMillis DESC LIMIT 1")
    fun observeLatest(): Flow<SessionWithExercises?>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE status = 'COMPLETED'")
    fun observeCompletedCount(): Flow<Int>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SessionWithExercises?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<SessionExerciseEntity>)

    @Transaction
    suspend fun insertSessionWithExercises(
        session: WorkoutSessionEntity,
        exercises: List<SessionExerciseEntity>,
    ) {
        insertSession(session)
        insertExercises(exercises)
    }

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAll()
}
