package com.jnetai.petcare.ui.pet

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.jnetai.petcare.data.entity.Pet
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityAddEditPetBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddEditPetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPetBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private var dateOfBirth: Long = System.currentTimeMillis()
    private var photoPath: String? = null
    private var currentPhotoUri: Uri? = null

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            copyPhotoToLocal(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImageLauncher.launch("image/*")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        petId = intent.getLongExtra("petId", -1)
        if (petId != -1L) loadPet()

        binding.textDob.setOnClickListener { showDatePicker() }
        binding.buttonSelectPhoto.setOnClickListener { selectPhoto() }
        binding.buttonSave.setOnClickListener { savePet() }
    }

    private fun loadPet() {
        lifecycleScope.launch {
            val pet = withContext(Dispatchers.IO) { repository.getPetById(petId) }
            pet?.let {
                binding.editName.setText(it.name)
                binding.editSpecies.setText(it.species)
                binding.editBreed.setText(it.breed)
                dateOfBirth = it.dateOfBirth
                binding.textDob.text = dateFormat.format(Date(it.dateOfBirth))
                photoPath = it.photoPath
                it.photoPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) binding.imagePet.setImageURI(Uri.fromFile(file))
                }
                supportActionBar?.title = "Edit Pet"
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateOfBirth
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                dateOfBirth = cal.timeInMillis
                binding.textDob.text = dateFormat.format(Date(dateOfBirth))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun selectPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun copyPhotoToLocal(uri: Uri) {
        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) {
                val dir = File(filesDir, "pet_photos")
                if (!dir.exists()) dir.mkdirs()
                val photoFile = File(dir, "${System.currentTimeMillis()}.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    photoFile.outputStream().use { output -> input.copyTo(output) }
                }
                photoFile.absolutePath
            }
            photoPath = file
            binding.imagePet.setImageURI(Uri.fromFile(File(file)))
        }
    }

    private fun savePet() {
        val name = binding.editName.text.toString().trim()
        val species = binding.editSpecies.text.toString().trim()
        val breed = binding.editBreed.text.toString().trim()

        if (name.isBlank()) {
            binding.editName.error = "Name required"
            return
        }
        if (species.isBlank()) {
            binding.editSpecies.error = "Species required"
            return
        }

        lifecycleScope.launch {
            if (petId == -1L) {
                val pet = Pet(
                    name = name,
                    species = species,
                    breed = breed,
                    dateOfBirth = dateOfBirth,
                    photoPath = photoPath
                )
                withContext(Dispatchers.IO) { repository.insertPet(pet) }
            } else {
                val existing = withContext(Dispatchers.IO) { repository.getPetById(petId) }
                existing?.let {
                    val updated = it.copy(
                        name = name,
                        species = species,
                        breed = breed,
                        dateOfBirth = dateOfBirth,
                        photoPath = photoPath
                    )
                    withContext(Dispatchers.IO) { repository.updatePet(updated) }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddEditPetActivity, "Pet saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}