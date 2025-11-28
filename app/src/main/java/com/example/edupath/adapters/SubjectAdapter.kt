package com.example.edupath.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edupath.data.Subject
import com.example.edupath.databinding.ItemSubjectBinding

class SubjectsAdapter : ListAdapter<Subject, SubjectsAdapter.SubjectViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = getItem(position)
        holder.bind(subject)
    }

    class SubjectViewHolder(private val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.tvSubjectScore.text = "${subject.score}%"
            binding.progressBarScore.progress = subject.score

            // Color code based on performance
            val color = when (subject.score) {
                in 80..100 -> Color.parseColor("#4CAF50") // Green
                in 70..79 -> Color.parseColor("#2196F3")  // Blue
                in 60..69 -> Color.parseColor("#FF9800")  // Orange
                in 50..59 -> Color.parseColor("#FFC107")  // Yellow
                else -> Color.parseColor("#F44336")       // Red
            }

            binding.progressBarScore.progressTintList = android.content.res.ColorStateList.valueOf(color)

            // Show core subject indicator
            if (subject.isCore) {
                binding.tvCoreSubject.visibility = android.view.View.VISIBLE
            } else {
                binding.tvCoreSubject.visibility = android.view.View.GONE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem == newItem
        }
    }
}