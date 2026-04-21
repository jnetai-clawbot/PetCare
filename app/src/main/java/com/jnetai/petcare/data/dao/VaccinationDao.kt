package com.jnetai.petcare.data.dao

import androidx.room.*
import com.jnetai.petcare.data.entity.Vaccination
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations WHERE petId = :petId ORDER BY date DESC")
    fun getByPet(petId: Long): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE reminderEnabled = 1 AND nextDueDate IS NOT NULL AND nextDueDate <= :threshold")
    suspend fun getDueOrOverdue(threshold: Long): List<Vaccination>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vaccination: Vaccination): Long

    @Update
    suspend fun update(vaccination: Vaccination)

    @Delete
    suspend fun delete(vaccination: Vaccination)

    @Query("SELECT * FROM vaccinations WHERE petId = :petId")
    suspend fun getByPetSync(petId: Long): List<Vaccination>
}