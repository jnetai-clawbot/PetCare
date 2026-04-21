package com.jnetai.petcare.data.dao

import androidx.room.*
import com.jnetai.petcare.data.entity.VetAppointment
import kotlinx.coroutines.flow.Flow

@Dao
interface VetAppointmentDao {
    @Query("SELECT * FROM vet_appointments WHERE petId = :petId ORDER BY dateTime ASC")
    fun getByPet(petId: Long): Flow<List<VetAppointment>>

    @Query("SELECT * FROM vet_appointments WHERE reminderEnabled = 1 AND dateTime > :now")
    suspend fun getUpcomingReminders(now: Long): List<VetAppointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: VetAppointment): Long

    @Update
    suspend fun update(appointment: VetAppointment)

    @Delete
    suspend fun delete(appointment: VetAppointment)

    @Query("SELECT * FROM vet_appointments WHERE petId = :petId")
    suspend fun getByPetSync(petId: Long): List<VetAppointment>
}