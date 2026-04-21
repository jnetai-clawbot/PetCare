package com.jnetai.petcare.ui.feeding

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.petcare.data.entity.FeedingSchedule
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityFeedingScheduleBinding
import com.jnetai.petcare.databinding.ItemFeedingScheduleBinding
import com.jnetai.petcare.ui.observe
import com.jnetai.petcare.util.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedingScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedingScheduleBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private lateinit var adapter: FeedingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedingScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Feeding Schedules"

        petId = intent.getLongExtra("petId", -1)

        adapter = FeedingAdapter(
            onToggle = { schedule, enabled ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.updateFeedingSchedule(schedule.copy(enabled = enabled)) }
                }
            },
            onDelete = { schedule -> showDeleteDialog(schedule) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        repository.getFeedingSchedules(petId).observe(this) { schedules ->
            adapter.submitList(schedules)
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(
            com.jnetai.petcare.R.layout.dialog_add_feeding, null
        )
        val foodInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editFoodName
        )
        val amountInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editAmount
        )
        val timeButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.jnetai.petcare.R.id.buttonTime
        )
        var hour = 8
        var minute = 0

        timeButton.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                hour = h
                minute = m
                timeButton.text = String.format("%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Feeding Schedule")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val food = foodInput.text.toString().trim()
                val amount = amountInput.text.toString().trim()
                if (food.isBlank()) {
                    Toast.makeText(this, "Food name required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val pet = withContext(Dispatchers.IO) { repository.getPetById(petId) }
                    val schedule = FeedingSchedule(
                        petId = petId,
                        foodName = food,
                        amount = amount,
                        hour = hour,
                        minute = minute
                    )
                    val id = withContext(Dispatchers.IO) { repository.insertFeedingSchedule(schedule) }
                    if (pet != null) {
                        ReminderScheduler.scheduleFeedingReminder(
                            this@FeedingScheduleActivity, id, pet.name, food, hour, minute
                        )
                    }
                    Toast.makeText(this@FeedingScheduleActivity, "Schedule added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(schedule: FeedingSchedule) {
        AlertDialog.Builder(this)
            .setTitle("Delete Schedule")
            .setMessage("Delete ${schedule.foodName} schedule?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.deleteFeedingSchedule(schedule) }
                    ReminderScheduler.cancelFeedingReminder(this@FeedingScheduleActivity, schedule.id)
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

class FeedingAdapter(
    private val onToggle: (FeedingSchedule, Boolean) -> Unit,
    private val onDelete: (FeedingSchedule) -> Unit
) : RecyclerView.Adapter<FeedingAdapter.ViewHolder>() {

    private var items = listOf<FeedingSchedule>()

    fun submitList(newItems: List<FeedingSchedule>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemFeedingScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemFeedingScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: FeedingSchedule) {
            binding.textFood.text = schedule.foodName
            binding.textAmount.text = schedule.amount
            binding.textTime.text = String.format("%02d:%02d", schedule.hour, schedule.minute)
            binding.switchEnabled.isChecked = schedule.enabled
            binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onToggle(schedule, isChecked)
            }
            binding.buttonDelete.setOnClickListener { onDelete(schedule) }
        }
    }
}