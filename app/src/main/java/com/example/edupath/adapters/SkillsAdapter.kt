package com.example.edupath.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edupath.databinding.ItemSkillBinding

class SkillsAdapter(
    private val onSkillsUpdated: (List<String>) -> Unit
) : ListAdapter<String, SkillsAdapter.SkillViewHolder>(DiffCallback) {

    private val selectedSkills = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val binding = ItemSkillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SkillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = getItem(position)
        val isSelected = selectedSkills.contains(skill)
        holder.bind(skill, isSelected)

        holder.itemView.setOnClickListener {
            if (selectedSkills.contains(skill)) {
                selectedSkills.remove(skill)
            } else {
                selectedSkills.add(skill)
            }
            notifyItemChanged(position)
            onSkillsUpdated(selectedSkills.toList())
        }
    }

    fun setSelectedSkills(skills: List<String>) {
        selectedSkills.clear()
        selectedSkills.addAll(skills)
        notifyDataSetChanged()
    }

    fun getSelectedSkills(): List<String> {
        return selectedSkills.toList()
    }

    inner class SkillViewHolder(private val binding: ItemSkillBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(skill: String, isSelected: Boolean) {
            binding.tvSkillName.text = skill

            // Update visual selection state safely
            val context = binding.root.context
            if (isSelected) {
                binding.cardSkill.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light))
                binding.ivCheck.visibility = View.VISIBLE
            } else {
                binding.cardSkill.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                binding.ivCheck.visibility = View.GONE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}