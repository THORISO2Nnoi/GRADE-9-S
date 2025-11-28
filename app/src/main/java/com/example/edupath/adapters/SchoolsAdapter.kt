package com.example.edupath.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.edupath.data.School
import com.example.edupath.databinding.ItemSchoolBinding

class SchoolsAdapter : ListAdapter<School, SchoolsAdapter.SchoolViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val binding = ItemSchoolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SchoolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        val school = getItem(position)
        holder.bind(school)
    }

    class SchoolViewHolder(private val binding: ItemSchoolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(school: School) {
            binding.tvSchoolName.text = school.name
            binding.tvSchoolLocation.text = "${school.location} • ${school.distance}"
            binding.tvSchoolType.text = school.type

            // Show streams
            val streamsText = school.streams.joinToString(", ")
            binding.tvStreams.text = "Streams: $streamsText"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<School>() {
        override fun areItemsTheSame(oldItem: School, newItem: School): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: School, newItem: School): Boolean {
            return oldItem == newItem
        }
    }
}