package com.jnetai.petcare.util

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jnetai.petcare.data.repository.PetCareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataExporter {

    suspend fun exportPetAsJson(context: Context, repository: PetCareRepository, petId: Long): String =
        withContext(Dispatchers.IO) {
            val pet = repository.getPetById(petId) ?: return@withContext "{}"
            val gson = GsonBuilder().setPrettyPrinting().create()
            val root = JsonObject()

            root.addProperty("name", pet.name)
            root.addProperty("species", pet.species)
            root.addProperty("breed", pet.breed)
            root.addProperty("dateOfBirth", pet.dateOfBirth)
            root.addProperty("photoPath", pet.photoPath)

            // Feeding schedules
            val feedings = JsonArray()
            repository.getFeedingSchedulesSync(petId).forEach { schedule ->
                val obj = JsonObject()
                obj.addProperty("foodName", schedule.foodName)
                obj.addProperty("amount", schedule.amount)
                obj.addProperty("time", "${schedule.hour}:${String.format("%02d", schedule.minute)}")
                obj.addProperty("enabled", schedule.enabled)
                feedings.add(obj)
            }
            root.add("feedingSchedules", feedings)

            // Vet appointments
            val appointments = JsonArray()
            repository.getVetAppointmentsSync(petId).forEach { apt ->
                val obj = JsonObject()
                obj.addProperty("vetName", apt.vetName)
                obj.addProperty("clinicName", apt.clinicName)
                obj.addProperty("reason", apt.reason)
                obj.addProperty("dateTime", apt.dateTime)
                obj.addProperty("notes", apt.notes)
                appointments.add(obj)
            }
            root.add("vetAppointments", appointments)

            // Medications
            val medications = JsonArray()
            repository.getMedicationsSync(petId).forEach { med ->
                val obj = JsonObject()
                obj.addProperty("name", med.name)
                obj.addProperty("dose", med.dose)
                obj.addProperty("frequency", med.frequency)
                obj.addProperty("startDate", med.startDate)
                obj.addProperty("endDate", med.endDate)
                obj.addProperty("notes", med.notes)
                medications.add(obj)
            }
            root.add("medications", medications)

            // Weight records
            val weights = JsonArray()
            repository.getWeightRecordsSync(petId).forEach { record ->
                val obj = JsonObject()
                obj.addProperty("weightKg", record.weightKg)
                obj.addProperty("date", record.date)
                obj.addProperty("notes", record.notes)
                weights.add(obj)
            }
            root.add("weightRecords", weights)

            // Vaccinations
            val vaccinations = JsonArray()
            repository.getVaccinationsSync(petId).forEach { vac ->
                val obj = JsonObject()
                obj.addProperty("name", vac.name)
                obj.addProperty("date", vac.date)
                obj.addProperty("nextDueDate", vac.nextDueDate)
                obj.addProperty("vetName", vac.vetName)
                obj.addProperty("notes", vac.notes)
                vaccinations.add(obj)
            }
            root.add("vaccinations", vaccinations)

            // Health notes
            val notes = JsonArray()
            repository.getHealthNotesSync(petId).forEach { note ->
                val obj = JsonObject()
                obj.addProperty("title", note.title)
                obj.addProperty("content", note.content)
                obj.addProperty("date", note.date)
                obj.addProperty("category", note.category)
                notes.add(obj)
            }
            root.add("healthNotes", notes)

            gson.toJson(root)
        }

    suspend fun exportAllPetsAsJson(context: Context, repository: PetCareRepository): String =
        withContext(Dispatchers.IO) {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val root = JsonArray()
            val pets = repository.getAllPetsSync()
            for (pet in pets) {
                val petJson = exportPetAsJson(context, repository, pet.id)
                root.add(gson.fromJson(petJson, JsonObject::class.java))
            }
            gson.toJson(root)
        }
}