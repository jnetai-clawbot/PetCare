package com.jnetai.petcare

import android.app.Application
import com.jnetai.petcare.data.PetCareDatabase
import com.jnetai.petcare.data.repository.PetCareRepository

class PetCareApp : Application() {
    val database by lazy { PetCareDatabase.getDatabase(this) }
    val repository by lazy {
        PetCareRepository(
            petDao = database.petDao(),
            feedingScheduleDao = database.feedingScheduleDao(),
            vetAppointmentDao = database.vetAppointmentDao(),
            medicationDao = database.medicationDao(),
            weightRecordDao = database.weightRecordDao(),
            vaccinationDao = database.vaccinationDao(),
            healthNoteDao = database.healthNoteDao()
        )
    }
}