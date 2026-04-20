package com.studenthousing.app.ui.roommates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studenthousing.app.data.model.RoommateDto
import com.studenthousing.app.databinding.ItemRoommateBinding

class RoommateAdapter(
    private val onConnect: (RoommateDto) -> Unit,
    private val onSkip: (RoommateDto) -> Unit
) : ListAdapter<RoommateDto, RoommateAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoommateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onConnect, onSkip)
    }

    class ViewHolder(private val binding: ItemRoommateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RoommateDto, onConnect: (RoommateDto) -> Unit, onSkip: (RoommateDto) -> Unit) {
            val student = item.student
            binding.roommateName.text = "${student?.firstName ?: ""} ${student?.lastName ?: ""}".trim().ifBlank { "Student" }
            binding.roommateUniversity.text = item.university ?: student?.email ?: "University not set"
            binding.roommateDepartment.text = "Department: ${item.department ?: "N/A"}"
            binding.roommateBudget.visibility = android.view.View.GONE

            binding.btnConnect.setOnClickListener { onConnect(item) }
            binding.btnSkip.setOnClickListener { onSkip(item) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RoommateDto>() {
        override fun areItemsTheSame(oldItem: RoommateDto, newItem: RoommateDto) = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: RoommateDto, newItem: RoommateDto) = oldItem == newItem
    }
}
