package com.jnetai.petcare.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.jnetai.petcare.util.Converters

@Entity(tableName = "pets")
@TypeConverters(Converters::class)
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String,
    val dateOfBirth: Long, // epoch millis
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)