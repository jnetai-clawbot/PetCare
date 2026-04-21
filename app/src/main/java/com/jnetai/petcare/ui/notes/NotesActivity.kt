package com.jnetai.petcare.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.petcare.data.entity.HealthNote
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityNotesBinding
import com.jnetai.petcare.databinding.ItemHealthNoteBinding
import com.jnetai.petcare.ui.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var repository: PetCareRepository
    private var petId: Long = -1
    private lateinit var adapter: NotesAdapter

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = (application as com.jnetai.petcare.PetCareApp).repository
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Health Notes"

        petId = intent.getLongExtra("petId", -1)

        adapter = NotesAdapter(
            onEdit = { note -> showEditDialog(note) },
            onDelete = { note -> showDeleteDialog(note) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        repository.getHealthNotes(petId).observe(this) { notes ->
            adapter.submitList(notes)
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(
            com.jnetai.petcare.R.layout.dialog_add_note, null
        )
        val titleInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editTitle
        )
        val contentInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editContent
        )
        val categorySpinner = dialogView.findViewById<android.widget.Spinner>(
            com.jnetai.petcare.R.id.spinnerCategory
        )

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val content = contentInput.text.toString().trim()
                val category = categorySpinner.selectedItem?.toString() ?: "General"
                if (title.isBlank()) {
                    Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val note = HealthNote(
                        petId = petId,
                        title = title,
                        content = content,
                        category = category
                    )
                    withContext(Dispatchers.IO) { repository.insertHealthNote(note) }
                    Toast.makeText(this@NotesActivity, "Note added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(note: HealthNote) {
        val dialogView = layoutInflater.inflate(
            com.jnetai.petcare.R.layout.dialog_add_note, null
        )
        val titleInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editTitle
        )
        val contentInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.jnetai.petcare.R.id.editContent
        )
        val categorySpinner = dialogView.findViewById<android.widget.Spinner>(
            com.jnetai.petcare.R.id.spinnerCategory
        )

        titleInput.setText(note.title)
        contentInput.setText(note.content)

        AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val content = contentInput.text.toString().trim()
                val category = categorySpinner.selectedItem?.toString() ?: "General"
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        repository.updateHealthNote(note.copy(title = title, content = content, category = category))
                    }
                    Toast.makeText(this@NotesActivity, "Note updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(note: HealthNote) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Delete \"${note.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repository.deleteHealthNote(note) }
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

class NotesAdapter(
    private val onEdit: (HealthNote) -> Unit,
    private val onDelete: (HealthNote) -> Unit
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    private var items = listOf<HealthNote>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun submitList(newItems: List<HealthNote>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemHealthNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemHealthNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: HealthNote) {
            binding.textTitle.text = note.title
            binding.textContent.text = note.content
            binding.textCategory.text = note.category
            binding.textDate.text = dateFormat.format(Date(note.date))
            binding.buttonEdit.setOnClickListener { onEdit(note) }
            binding.buttonDelete.setOnClickListener { onDelete(note) }
        }
    }
}