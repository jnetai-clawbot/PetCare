package com.jnetai.petcare.ui.weight

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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.jnetai.petcare.data.entity.WeightRecord
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityWeightTrackingBinding
import com.jnetai.petcare.databinding.ItemWeightRecordBinding
import com.jnetai.petcare.ui.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeightTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeightTrackingBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private lateinit var adapter: WeightAdapter

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeightTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Weight Tracking"

        petId = intent.getLongExtra("petId", -1)

        adapter = WeightAdapter { record -> showDeleteDialog(record) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        repository.getWeightRecords(petId).observe(this) { records ->
            adapter.submitList(records)
            updateChart(records)
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun updateChart(records: List<WeightRecord>) {
        if (records.isEmpty()) {
            binding.chart.visibility = android.view.View.GONE
            return
        }
        binding.chart.visibility = android.view.View.VISIBLE

        val entries = records.mapIndexed { index, record ->
            Entry(index.toFloat(), record.weightKg)
        }

        val dataSet = LineDataSet(entries, "Weight (kg)").apply {
            color = getColor(com.jnetai.petcare.R.color.purple_500)
            setDrawValues(true)
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 4f
            valueTextSize = 10f
        }

        binding.chart.data = LineData(dataSet)
        binding.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in records.indices) dateFormat.format(Date(records[index].date))
                else ""
            }
        }
        binding.chart.xAxis.setLabelCount(records.size.coerceAtMost(8), false)
        binding.chart.xAxis.setGranularity(1f)
        binding.chart.description.isEnabled = false
        binding.chart.invalidate()
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(
            com.jnetai.petcare.R.layout.dialog_add_weight, null
        )
        val weightInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editWeight
        )
        val dateButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.jnetai.petcare.R.id.buttonDate
        )

        var selectedDate = System.currentTimeMillis()
        dateButton.text = dateFormat.format(Date(selectedDate))
        dateButton.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                selectedDate = cal.timeInMillis
                dateButton.text = dateFormat.format(Date(selectedDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Weight Record")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val weightStr = weightInput.text.toString().trim()
                if (weightStr.isBlank()) {
                    Toast.makeText(this, "Weight required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val weight = weightStr.toFloatOrNull()
                if (weight == null) {
                    Toast.makeText(this, "Invalid weight", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val record = WeightRecord(
                        petId = petId,
                        weightKg = weight,
                        date = selectedDate
                    )
                    withContext(Dispatchers.IO) { repository.insertWeightRecord(record) }
                    Toast.makeText(this@WeightTrackingActivity, "Weight recorded", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(record: WeightRecord) {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Delete this weight record?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.deleteWeightRecord(record) }
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

class WeightAdapter(private val onDelete: (WeightRecord) -> Unit) :
    RecyclerView.Adapter<WeightAdapter.ViewHolder>() {

    private var items = listOf<WeightRecord>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun submitList(newItems: List<WeightRecord>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemWeightRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemWeightRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: WeightRecord) {
            binding.textWeight.text = "${record.weightKg} kg"
            binding.textDate.text = dateFormat.format(Date(record.date))
            binding.buttonDelete.setOnClickListener { onDelete(record) }
        }
    }
}