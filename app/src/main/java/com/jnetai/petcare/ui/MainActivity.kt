package com.jnetai.petcare.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.petcare.R
import com.jnetai.petcare.data.entity.Pet
import com.jnetai.petcare.data.repository.PetCareRepository
import com.jnetai.petcare.databinding.ActivityMainBinding
import com.jnetai.petcare.ui.about.AboutActivity
import com.jnetai.petcare.ui.pet.AddEditPetActivity
import com.jnetai.petcare.ui.pet.PetAdapter
import com.jnetai.petcare.ui.pet.PetDetailActivity
import com.jnetai.petcare.util.DataExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: PetCareRepository
    private lateinit var adapter: PetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "PetCare"

        repository = (application as com.jnetai.petcare.PetCareApp).repository

        adapter = PetAdapter { pet ->
            startActivity(Intent(this, PetDetailActivity::class.java).apply {
                putExtra("petId", pet.id)
            })
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        repository.getAllPets().observe(this) { pets ->
            adapter.submitList(pets)
            binding.emptyView.visibility = if (pets.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.recyclerView.visibility = if (pets.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        }

        binding.fabAddPet.setOnClickListener {
            startActivity(Intent(this, AddEditPetActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.action_export_all -> {
                exportAllPets()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportAllPets() {
        lifecycleScope.launch {
            val json = DataExporter.exportAllPetsAsJson(this@MainActivity, repository)
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_TEXT, json)
                    putExtra(Intent.EXTRA_SUBJECT, "PetCare - All Pets Data")
                }
                startActivity(Intent.createChooser(intent, "Export Pet Data"))
            }
        }
    }
}