package com.jnetai.petcare.data.dao

import androidx.room.*
import com.jnetai.petcare.data.entity.FeedingSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingScheduleDao {
    @Query("SELECT * FROM feeding_schedules WHERE petId = :petId ORDER BY hour, minute")
    fun getByPet(petId: Long): Flow<List<FeedingSchedule>>

    @Query("SELECT * FROM feeding_schedules WHERE reminderEnabled = 1 AND enabled = 1")
    suspend fun getEnabledReminders(): List<FeedingSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: FeedingSchedule): Long

    @Update
    suspend fun update(schedule: FeedingSchedule)

    @Delete
    suspend fun delete(schedule: FeedingSchedule)

    @Query("SELECT * FROM feeding_schedules WHERE petId = :petId")
    suspend fun getByPetSync(petId: Long): List<FeedingSchedule>
}