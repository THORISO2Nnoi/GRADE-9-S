package com.example.edupath.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edupath.data.Recommendation
import com.example.edupath.databinding.ItemRecommendationBinding

class RecommendationsAdapter : ListAdapter<Recommendation, RecommendationsAdapter.RecommendationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = getItem(position)
        holder.bind(recommendation)
    }

    class RecommendationViewHolder(private val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recommendation: Recommendation) {
            binding.tvRecommendationTitle.text = recommendation.title
            binding.tvRecommendationDescription.text = recommendation.description

            // Show requirements if available
            if (recommendation.requirements.isNotEmpty()) {
                binding.tvRequirements.visibility = android.view.View.VISIBLE
                val requirementsText = recommendation.requirements.joinToString("\n• ", "• ")
                binding.tvRequirements.text = requirementsText
            } else {
                binding.tvRequirements.visibility = android.view.View.GONE
            }

            // Show type badge
            binding.tvType.text = recommendation.type.name.replace("_", " ")
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Recommendation>() {
        override fun areItemsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
            return oldItem.title == newItem.title && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
            return oldItem == newItem
        }
    }
}