package com.jnetai.petcare.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jnetai.petcare.BuildConfig
import com.jnetai.petcare.R
import com.jnetai.petcare.databinding.ActivityAboutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About"

        binding.textAppName.text = "PetCare"
        binding.textVersion.text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        binding.textDescription.text = "A comprehensive pet care coordinator. Track feeding, vet visits, medications, weight, vaccinations, and health notes for all your pets."

        binding.buttonCheckUpdates.setOnClickListener {
            checkForUpdates()
        }

        binding.buttonShare.setOnClickListener {
            shareApp()
        }

        binding.buttonGitHub.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jnetai-clawbot/PetCare")))
        }
    }

    private fun checkForUpdates() {
        binding.textUpdateStatus.text = "Checking..."
        binding.textUpdateStatus.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val url = URL("https://api.github.com/repos/jnetai-clawbot/PetCare/releases/latest")
                    val connection = url.openConnection()
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    connection.connect()
                    val response = connection.getInputStream().bufferedReader().readText()
                    val json = JSONObject(response)
                    json.getString("tag_name") to json.optString("html_url", "")
                }

                val latestVersion = result.first.trimStart('v')
                val currentVersion = BuildConfig.VERSION_NAME

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (latestVersion != currentVersion) {
                        binding.textUpdateStatus.text = "Update available: v$latestVersion"
                        binding.textUpdateStatus.setTextColor(getColor(android.R.color.holo_green_light))
                        binding.buttonDownloadUpdate.visibility = View.VISIBLE
                        binding.buttonDownloadUpdate.setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.second)))
                        }
                    } else {
                        binding.textUpdateStatus.text = "You're up to date! (v$currentVersion)"
                        binding.textUpdateStatus.setTextColor(getColor(android.R.color.holo_green_light))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.textUpdateStatus.text = "Failed to check: ${e.message}"
                    binding.textUpdateStatus.setTextColor(getColor(android.R.color.holo_red_light))
                }
            }
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "PetCare - Pet Care Coordinator")
            putExtra(Intent.EXTRA_TEXT, "Check out PetCare - a comprehensive pet care coordinator app!\n\nhttps://github.com/jnetai-clawbot/PetCare")
        }
        startActivity(Intent.createChooser(intent, "Share PetCare"))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}