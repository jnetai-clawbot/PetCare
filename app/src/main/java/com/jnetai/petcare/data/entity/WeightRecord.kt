package com.jnetai.petcare.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "weight_records",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class WeightRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val weightKg: Float,
    val date: Long, // epoch millis
    val notes: String = ""
)