package com.jnetai.petcare.data.dao

import androidx.room.*
import com.jnetai.petcare.data.entity.HealthNote
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthNoteDao {
    @Query("SELECT * FROM health_notes WHERE petId = :petId ORDER BY date DESC")
    fun getByPet(petId: Long): Flow<List<HealthNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: HealthNote): Long

    @Update
    suspend fun update(note: HealthNote)

    @Delete
    suspend fun delete(note: HealthNote)

    @Query("SELECT * FROM health_notes WHERE petId = :petId")
    suspend fun getByPetSync(petId: Long): List<HealthNote>
}