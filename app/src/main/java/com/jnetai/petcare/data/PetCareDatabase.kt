package com.jnetai.petcare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jnetai.petcare.data.dao.*
import com.jnetai.petcare.data.entity.*
import com.jnetai.petcare.util.Converters

@Database(
    entities = [
        Pet::class,
        FeedingSchedule::class,
        VetAppointment::class,
        Medication::class,
        WeightRecord::class,
        Vaccination::class,
        HealthNote::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PetCareDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun feedingScheduleDao(): FeedingScheduleDao
    abstract fun vetAppointmentDao(): VetAppointmentDao
    abstract fun medicationDao(): MedicationDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun healthNoteDao(): HealthNoteDao

    companion object {
        @Volatile
        private var INSTANCE: PetCareDatabase? = null

        fun getDatabase(context: Context): PetCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetCareDatabase::class.java,
                    "petcare_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}