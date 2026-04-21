package com.jnetai.petcare.ui.pet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.petcare.data.entity.Pet
import com.jnetai.petcare.databinding.ItemPetBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PetAdapter(private val onClick: (Pet) -> Unit) :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    private var pets = listOf<Pet>()

    fun submitList(newPets: List<Pet>) {
        val old = pets
        pets = newPets
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = old.size
            override fun getNewListSize() = newPets.size
            override fun areItemsTheSame(i: Int, j: Int) = old[i].id == newPets[j].id
            override fun areContentsTheSame(i: Int, j: Int) = old[i] == newPets[j]
        }).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PetViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(pets[position])
    }

    override fun getItemCount() = pets.size

    class PetViewHolder(
        private val binding: ItemPetBinding,
        private val onClick: (Pet) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(pet: Pet) {
            binding.textPetName.text = pet.name
            binding.textSpeciesBreed.text = "${pet.species} • ${pet.breed}"
            binding.textDob.text = "Born: ${dateFormat.format(Date(pet.dateOfBirth))}"

            pet.photoPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    binding.imagePet.setImageURI(android.net.Uri.fromFile(file))
                } else {
                    binding.imagePet.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } ?: binding.imagePet.setImageResource(android.R.drawable.ic_menu_gallery)

            binding.root.setOnClickListener { onClick(pet) }
        }
    }
}