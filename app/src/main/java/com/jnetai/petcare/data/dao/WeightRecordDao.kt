package com.jnetai.petcare.data.dao

import androidx.room.*
import com.jnetai.petcare.data.entity.WeightRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightRecordDao {
    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY date ASC")
    fun getByPet(petId: Long): Flow<List<WeightRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WeightRecord): Long

    @Delete
    suspend fun delete(record: WeightRecord)

    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY date ASC")
    suspend fun getByPetSync(petId: Long): List<WeightRecord>

    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(petId: Long): WeightRecord?
}