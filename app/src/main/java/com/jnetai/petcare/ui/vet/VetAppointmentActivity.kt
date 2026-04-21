package com.jnetai.petcare.ui.vet

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.petcare.data.entity.VetAppointment
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityVetAppointmentBinding
import com.jnetai.petcare.databinding.ItemVetAppointmentBinding
import com.jnetai.petcare.ui.observe
import com.jnetai.petcare.util.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class VetAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVetAppointmentBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private lateinit var adapter: VetAdapter

    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVetAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Vet Appointments"

        petId = intent.getLongExtra("petId", -1)

        adapter = VetAdapter { appointment -> showDeleteDialog(appointment) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        repository.getVetAppointments(petId).observe(this) { appointments ->
            adapter.submitList(appointments)
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(
            com.jnetai.petcare.R.layout.dialog_add_vet, null
        )
        val vetInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editVetName
        )
        val clinicInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editClinicName
        )
        val reasonInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editReason
        )
        val dateButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.jnetai.petcare.R.id.buttonDate
        )

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        var selectedTime = cal.timeInMillis

        dateButton.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(Calendar.YEAR, y)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.DAY_OF_MONTH, d)
                TimePickerDialog(this, { _, h, min ->
                    cal.set(Calendar.HOUR_OF_DAY, h)
                    cal.set(Calendar.MINUTE, min)
                    selectedTime = cal.timeInMillis
                    dateButton.text = dateFormat.format(Date(selectedTime))
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        dateButton.text = dateFormat.format(Date(selectedTime))

        AlertDialog.Builder(this)
            .setTitle("Add Vet Appointment")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val vetName = vetInput.text.toString().trim()
                val clinicName = clinicInput.text.toString().trim()
                val reason = reasonInput.text.toString().trim()
                if (vetName.isBlank()) {
                    Toast.makeText(this, "Vet name required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val pet = withContext(Dispatchers.IO) { repository.getPetById(petId) }
                    val appointment = VetAppointment(
                        petId = petId,
                        vetName = vetName,
                        clinicName = clinicName,
                        reason = reason,
                        dateTime = selectedTime
                    )
                    val id = withContext(Dispatchers.IO) { repository.insertVetAppointment(appointment) }
                    if (pet != null) {
                        ReminderScheduler.scheduleVetReminder(
                            this@VetAppointmentActivity, id, pet.name, vetName, selectedTime
                        )
                    }
                    Toast.makeText(this@VetAppointmentActivity, "Appointment added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(appointment: VetAppointment) {
        AlertDialog.Builder(this)
            .setTitle("Delete Appointment")
            .setMessage("Delete appointment with ${appointment.vetName}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.deleteVetAppointment(appointment) }
                    ReminderScheduler.cancelVetReminder(this@VetAppointmentActivity, appointment.id)
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

class VetAdapter(private val onDelete: (VetAppointment) -> Unit) :
    RecyclerView.Adapter<VetAdapter.ViewHolder>() {

    private var items = listOf<VetAppointment>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    fun submitList(newItems: List<VetAppointment>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemVetAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemVetAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(apt: VetAppointment) {
            binding.textVetName.text = apt.vetName
            binding.textClinic.text = apt.clinicName
            binding.textReason.text = apt.reason
            binding.textDateTime.text = dateFormat.format(Date(apt.dateTime))
            binding.buttonDelete.setOnClickListener { onDelete(apt) }
        }
    }
}