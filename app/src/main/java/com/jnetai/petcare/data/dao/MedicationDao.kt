package com.jnetai.petcare.data.dao

import androidx.room.*
import com.jnetai.petcare.data.entity.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE petId = :petId ORDER BY name ASC")
    fun getByPet(petId: Long): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE reminderEnabled = 1")
    suspend fun getEnabledReminders(): List<Medication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("SELECT * FROM medications WHERE petId = :petId")
    suspend fun getByPetSync(petId: Long): List<Medication>
}