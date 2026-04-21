package com.jnetai.petcare.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "medications",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val name: String,
    val dose: String,
    val frequency: String, // e.g. "Once daily", "Twice daily", "Weekly"
    val startDate: Long,
    val endDate: Long? = null,
    val reminderEnabled: Boolean = true,
    val notes: String = ""
)