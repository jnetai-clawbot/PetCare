package com.jnetai.petcare.ui.medication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.petcare.data.entity.Medication
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityMedicationBinding
import com.jnetai.petcare.databinding.ItemMedicationBinding
import com.jnetai.petcare.ui.observe
import com.jnetai.petcare.util.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MedicationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicationBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private lateinit var adapter: MedicationAdapter

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Medications"

        petId = intent.getLongExtra("petId", -1)

        adapter = MedicationAdapter { med -> showDeleteDialog(med) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        repository.getMedications(petId).observe(this) { meds ->
            adapter.submitList(meds)
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(
            com.jnetai.petcare.R.layout.dialog_add_medication, null
        )
        val nameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editMedName
        )
        val doseInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editDose
        )
        val freqInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editFrequency
        )

        AlertDialog.Builder(this)
            .setTitle("Add Medication")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val dose = doseInput.text.toString().trim()
                val frequency = freqInput.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(this, "Medication name required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val pet = withContext(Dispatchers.IO) { repository.getPetById(petId) }
                    val medication = Medication(
                        petId = petId,
                        name = name,
                        dose = dose,
                        frequency = frequency,
                        startDate = System.currentTimeMillis()
                    )
                    val id = withContext(Dispatchers.IO) { repository.insertMedication(medication) }
                    if (pet != null) {
                        ReminderScheduler.scheduleMedicationReminder(
                            this@MedicationActivity, id, pet.name, name, dose
                        )
                    }
                    Toast.makeText(this@MedicationActivity, "Medication added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(med: Medication) {
        AlertDialog.Builder(this)
            .setTitle("Delete Medication")
            .setMessage("Delete ${med.name}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.deleteMedication(med) }
                    ReminderScheduler.cancelMedicationReminder(this@MedicationActivity, med.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class MedicationAdapter(private val onDelete: (Medication) -> Unit) :
    RecyclerView.Adapter<MedicationAdapter.ViewHolder>() {

    private var items = listOf<Medication>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun submitList(newItems: List<Medication>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemMedicationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(med: Medication) {
            binding.textMedName.text = med.name
            binding.textDose.text = "Dose: ${med.dose}"
            binding.textFrequency.text = med.frequency
            binding.textStartDate.text = "Since: ${dateFormat.format(Date(med.startDate))}"
            binding.buttonDelete.setOnClickListener { onDelete(med) }
        }
    }
}