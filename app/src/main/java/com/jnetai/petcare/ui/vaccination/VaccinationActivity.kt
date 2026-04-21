package com.jnetai.petcare.ui.vaccination

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.petcare.data.entity.Vaccination
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityVaccinationBinding
import com.jnetai.petcare.databinding.ItemVaccinationBinding
import com.jnetai.petcare.ui.observe
import com.jnetai.petcare.util.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class VaccinationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVaccinationBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private lateinit var adapter: VaccinationAdapter

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaccinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Vaccinations"

        petId = intent.getLongExtra("petId", -1)

        adapter = VaccinationAdapter { vac -> showDeleteDialog(vac) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        repository.getVaccinations(petId).observe(this) { vaccinations ->
            adapter.submitList(vaccinations)
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(
            com.jnetai.petcare.R.layout.dialog_add_vaccination, null
        )
        val nameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editVaccineName
        )
        val vetInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editVetName
        )
        val dateButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.jnetai.petcare.R.id.buttonDate
        )
        val dueDateButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.jnetai.petcare.R.id.buttonDueDate
        )

        var selectedDate = System.currentTimeMillis()
        var dueDate: Long? = null
        dateButton.text = dateFormat.format(Date(selectedDate))

        dateButton.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                selectedDate = cal.timeInMillis
                dateButton.text = dateFormat.format(Date(selectedDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        dueDateButton.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, 1)
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                dueDate = cal.timeInMillis
                dueDateButton.text = "Due: ${dateFormat.format(Date(dueDate!!))}"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Vaccination")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(this, "Vaccine name required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val vetName = vetInput.text.toString().trim()
                lifecycleScope.launch {
                    val pet = withContext(Dispatchers.IO) { repository.getPetById(petId) }
                    val vaccination = Vaccination(
                        petId = petId,
                        name = name,
                        date = selectedDate,
                        nextDueDate = dueDate,
                        vetName = vetName
                    )
                    val id = withContext(Dispatchers.IO) { repository.insertVaccination(vaccination) }
                    if (pet != null && dueDate != null) {
                        ReminderScheduler.scheduleVaccinationReminder(
                            this@VaccinationActivity, id, pet.name, name, dueDate!!
                        )
                    }
                    Toast.makeText(this@VaccinationActivity, "Vaccination added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(vac: Vaccination) {
        AlertDialog.Builder(this)
            .setTitle("Delete Vaccination")
            .setMessage("Delete ${vac.name} record?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.deleteVaccination(vac) }
                    ReminderScheduler.cancelVaccinationReminder(this@VaccinationActivity, vac.id)
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

class VaccinationAdapter(private val onDelete: (Vaccination) -> Unit) :
    RecyclerView.Adapter<VaccinationAdapter.ViewHolder>() {

    private var items = listOf<Vaccination>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun submitList(newItems: List<Vaccination>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemVaccinationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemVaccinationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vac: Vaccination) {
            binding.textVaccineName.text = vac.name
            binding.textDate.text = "Given: ${dateFormat.format(Date(vac.date))}"
            binding.textDueDate.text = vac.nextDueDate?.let {
                val isOverdue = it < System.currentTimeMillis()
                val prefix = if (isOverdue) "⚠️ OVERDUE: " else "Due: "
                "$prefix${dateFormat.format(Date(it))}"
            } ?: "No due date"
            binding.textVet.text = if (vac.vetName.isNotBlank()) "Vet: ${vac.vetName}" else ""
            binding.buttonDelete.setOnClickListener { onDelete(vac) }
        }
    }
}