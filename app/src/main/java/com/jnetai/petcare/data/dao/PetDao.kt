package com.jnetai.petcare.data.dao

import androidx.room.*
import com.jnetai.petcare.data.entity.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY name ASC")
    fun getAllPets(): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getPetById(id: Long): Pet?

    @Query("SELECT * FROM pets WHERE id = :id")
    fun getPetByIdFlow(id: Long): Flow<Pet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: Pet): Long

    @Update
    suspend fun update(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Query("SELECT * FROM pets")
    suspend fun getAllPetsSync(): List<Pet>
}