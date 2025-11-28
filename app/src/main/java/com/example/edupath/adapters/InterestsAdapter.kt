package com.example.edupath.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edupath.databinding.ItemInterestBinding

class InterestsAdapter(
    private val onInterestsUpdated: (List<String>) -> Unit
) : ListAdapter<String, InterestsAdapter.InterestViewHolder>(DiffCallback) {

    private val selectedInterests = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val binding = ItemInterestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InterestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        val interest = getItem(position)
        val isSelected = selectedInterests.contains(interest)
        holder.bind(interest, isSelected)

        holder.itemView.setOnClickListener {
            if (selectedInterests.contains(interest)) {
                selectedInterests.remove(interest)
            } else {
                selectedInterests.add(interest)
            }
            notifyItemChanged(position)
            onInterestsUpdated(selectedInterests.toList())
        }
    }

    class InterestViewHolder(private val binding: ItemInterestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(interest: String, isSelected: Boolean) {
            binding.tvInterestName.text = interest

            if (isSelected) {
                binding.cardInterest.setCardBackgroundColor(binding.root.context.getColor(android.R.color.holo_blue_light))
            } else {
                binding.cardInterest.setCardBackgroundColor(binding.root.context.getColor(android.R.color.white))
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