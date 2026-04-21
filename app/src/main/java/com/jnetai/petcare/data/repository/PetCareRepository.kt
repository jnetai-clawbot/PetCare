package com.jnetai.petcare.data.repository

import com.jnetai.petcare.data.dao.*
import com.jnetai.petcare.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PetCareRepository(
    private val petDao: PetDao,
    private val feedingScheduleDao: FeedingScheduleDao,
    private val vetAppointmentDao: VetAppointmentDao,
    private val medicationDao: MedicationDao,
    private val weightRecordDao: WeightRecordDao,
    private val vaccinationDao: VaccinationDao,
    private val healthNoteDao: HealthNoteDao
) {
    // Pets
    fun getAllPets(): Flow<List<Pet>> = petDao.getAllPets()
    suspend fun getPetById(id: Long) = withContext(Dispatchers.IO) { petDao.getPetById(id) }
    fun getPetByIdFlow(id: Long) = petDao.getPetByIdFlow(id)
    suspend fun insertPet(pet: Pet) = withContext(Dispatchers.IO) { petDao.insert(pet) }
    suspend fun updatePet(pet: Pet) = withContext(Dispatchers.IO) { petDao.update(pet) }
    suspend fun deletePet(pet: Pet) = withContext(Dispatchers.IO) { petDao.delete(pet) }
    suspend fun getAllPetsSync() = withContext(Dispatchers.IO) { petDao.getAllPetsSync() }

    // Feeding Schedules
    fun getFeedingSchedules(petId: Long) = feedingScheduleDao.getByPet(petId)
    suspend fun getEnabledFeedingReminders() = withContext(Dispatchers.IO) { feedingScheduleDao.getEnabledReminders() }
    suspend fun insertFeedingSchedule(schedule: FeedingSchedule) = withContext(Dispatchers.IO) { feedingScheduleDao.insert(schedule) }
    suspend fun updateFeedingSchedule(schedule: FeedingSchedule) = withContext(Dispatchers.IO) { feedingScheduleDao.update(schedule) }
    suspend fun deleteFeedingSchedule(schedule: FeedingSchedule) = withContext(Dispatchers.IO) { feedingScheduleDao.delete(schedule) }
    suspend fun getFeedingSchedulesSync(petId: Long) = withContext(Dispatchers.IO) { feedingScheduleDao.getByPetSync(petId) }

    // Vet Appointments
    fun getVetAppointments(petId: Long) = vetAppointmentDao.getByPet(petId)
    suspend fun getUpcomingVetReminders(now: Long) = withContext(Dispatchers.IO) { vetAppointmentDao.getUpcomingReminders(now) }
    suspend fun insertVetAppointment(appointment: VetAppointment) = withContext(Dispatchers.IO) { vetAppointmentDao.insert(appointment) }
    suspend fun updateVetAppointment(appointment: VetAppointment) = withContext(Dispatchers.IO) { vetAppointmentDao.update(appointment) }
    suspend fun deleteVetAppointment(appointment: VetAppointment) = withContext(Dispatchers.IO) { vetAppointmentDao.delete(appointment) }
    suspend fun getVetAppointmentsSync(petId: Long) = withContext(Dispatchers.IO) { vetAppointmentDao.getByPetSync(petId) }

    // Medications
    fun getMedications(petId: Long) = medicationDao.getByPet(petId)
    suspend fun getEnabledMedicationReminders() = withContext(Dispatchers.IO) { medicationDao.getEnabledReminders() }
    suspend fun insertMedication(medication: Medication) = withContext(Dispatchers.IO) { medicationDao.insert(medication) }
    suspend fun updateMedication(medication: Medication) = withContext(Dispatchers.IO) { medicationDao.update(medication) }
    suspend fun deleteMedication(medication: Medication) = withContext(Dispatchers.IO) { medicationDao.delete(medication) }
    suspend fun getMedicationsSync(petId: Long) = withContext(Dispatchers.IO) { medicationDao.getByPetSync(petId) }

    // Weight Records
    fun getWeightRecords(petId: Long) = weightRecordDao.getByPet(petId)
    suspend fun insertWeightRecord(record: WeightRecord) = withContext(Dispatchers.IO) { weightRecordDao.insert(record) }
    suspend fun deleteWeightRecord(record: WeightRecord) = withContext(Dispatchers.IO) { weightRecordDao.delete(record) }
    suspend fun getWeightRecordsSync(petId: Long) = withContext(Dispatchers.IO) { weightRecordDao.getByPetSync(petId) }
    suspend fun getLatestWeight(petId: Long) = withContext(Dispatchers.IO) { weightRecordDao.getLatest(petId) }

    // Vaccinations
    fun getVaccinations(petId: Long) = vaccinationDao.getByPet(petId)
    suspend fun getDueVaccinations(threshold: Long) = withContext(Dispatchers.IO) { vaccinationDao.getDueOrOverdue(threshold) }
    suspend fun insertVaccination(vaccination: Vaccination) = withContext(Dispatchers.IO) { vaccinationDao.insert(vaccination) }
    suspend fun updateVaccination(vaccination: Vaccination) = withContext(Dispatchers.IO) { vaccinationDao.update(vaccination) }
    suspend fun deleteVaccination(vaccination: Vaccination) = withContext(Dispatchers.IO) { vaccinationDao.delete(vaccination) }
    suspend fun getVaccinationsSync(petId: Long) = withContext(Dispatchers.IO) { vaccinationDao.getByPetSync(petId) }

    // Health Notes
    fun getHealthNotes(petId: Long) = healthNoteDao.getByPet(petId)
    suspend fun insertHealthNote(note: HealthNote) = withContext(Dispatchers.IO) { healthNoteDao.insert(note) }
    suspend fun updateHealthNote(note: HealthNote) = withContext(Dispatchers.IO) { healthNoteDao.update(note) }
    suspend fun deleteHealthNote(note: HealthNote) = withContext(Dispatchers.IO) { healthNoteDao.delete(note) }
    suspend fun getHealthNotesSync(petId: Long) = withContext(Dispatchers.IO) { healthNoteDao.getByPetSync(petId) }
}