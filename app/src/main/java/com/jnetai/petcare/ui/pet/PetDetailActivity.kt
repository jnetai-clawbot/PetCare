package com.jnetai.petcare.ui.pet

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jnetai.petcare.R
import com.jnetai.petcare.data.entity.Pet
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityPetDetailBinding
import com.jnetai.petcare.ui.feeding.FeedingScheduleActivity
import com.jnetai.petcare.ui.medication.MedicationActivity
import com.jnetai.petcare.ui.notes.NotesActivity
import com.jnetai.petcare.ui.vaccination.VaccinationActivity
import com.jnetai.petcare.ui.vet.VetAppointmentActivity
import com.jnetai.petcare.ui.weight.WeightTrackingActivity
import com.jnetai.petcare.util.DataExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PetDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPetDetailBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private var pet: Pet? = null

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        petId = intent.getLongExtra("petId", -1)
        loadPet()

        binding.cardFeeding.setOnClickListener {
            startActivity(Intent(this, FeedingScheduleActivity::class.java).apply {
                putExtra("petId", petId)
            })
        }
        binding.cardVet.setOnClickListener {
            startActivity(Intent(this, VetAppointmentActivity::class.java).apply {
                putExtra("petId", petId)
            })
        }
        binding.cardMedication.setOnClickListener {
            startActivity(Intent(this, MedicationActivity::class.java).apply {
                putExtra("petId", petId)
            })
        }
        binding.cardWeight.setOnClickListener {
            startActivity(Intent(this, WeightTrackingActivity::class.java).apply {
                putExtra("petId", petId)
            })
        }
        binding.cardVaccination.setOnClickListener {
            startActivity(Intent(this, VaccinationActivity::class.java).apply {
                putExtra("petId", petId)
            })
        }
        binding.cardNotes.setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java).apply {
                putExtra("petId", petId)
            })
        }
    }

    private fun loadPet() {
        lifecycleScope.launch {
            val p = withContext(Dispatchers.IO) { repository.getPetById(petId) }
            p?.let {
                pet = it
                supportActionBar?.title = it.name
                binding.textName.text = it.name
                binding.textSpeciesBreed.text = "${it.species} • ${it.breed}"
                binding.textDob.text = "Born: ${dateFormat.format(Date(it.dateOfBirth))}"
                it.photoPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) binding.imagePet.setImageURI(android.net.Uri.fromFile(file))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pet_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                startActivity(Intent(this, AddEditPetActivity::class.java).apply {
                    putExtra("petId", petId)
                })
                true
            }
            R.id.action_delete -> {
                deletePet()
                true
            }
            R.id.action_export -> {
                exportPet()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deletePet() {
        pet?.let { p ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) { repository.deletePet(p) }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PetDetailActivity, "Pet deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun exportPet() {
        lifecycleScope.launch {
            val json = DataExporter.exportPetAsJson(this@PetDetailActivity, repository, petId)
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_TEXT, json)
                    putExtra(Intent.EXTRA_SUBJECT, "PetCare - ${pet?.name ?: "Pet"} Data")
                }
                startActivity(Intent.createChooser(intent, "Export Pet Data"))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPet()
    }
}