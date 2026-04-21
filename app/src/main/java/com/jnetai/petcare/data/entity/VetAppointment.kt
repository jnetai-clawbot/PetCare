package com.jnetai.petcare.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "vet_appointments",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class VetAppointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val vetName: String,
    val clinicName: String,
    val reason: String,
    val dateTime: Long, // epoch millis
    val reminderEnabled: Boolean = true,
    val notes: String = ""
)