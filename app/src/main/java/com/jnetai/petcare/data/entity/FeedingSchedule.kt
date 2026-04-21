package com.jnetai.petcare.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "feeding_schedules",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("petId")]
)
data class FeedingSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val foodName: String,
    val amount: String,
    val hour: Int,    // 0-23
    val minute: Int,  // 0-59
    val enabled: Boolean = true,
    val reminderEnabled: Boolean = true
)