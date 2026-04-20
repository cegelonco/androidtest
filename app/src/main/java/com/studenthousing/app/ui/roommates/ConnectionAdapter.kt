package com.studenthousing.app.ui.roommates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studenthousing.app.data.model.ConnectionDto
import com.studenthousing.app.databinding.ItemConnectionBinding

class ConnectionAdapter : ListAdapter<ConnectionDto, ConnectionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConnectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemConnectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConnectionDto) {
            val student = item.user?.student
            binding.connectionName.text = "${student?.firstName ?: ""} ${student?.lastName ?: ""}".trim().ifBlank { "Student" }
            binding.connectionInfo.text = item.user?.university ?: "University not set"
            binding.connectionStatus.text = (item.status ?: "connected").replaceFirstChar { it.uppercase() }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ConnectionDto>() {
        override fun areItemsTheSame(oldItem: ConnectionDto, newItem: ConnectionDto) =
            oldItem.user?._id == newItem.user?._id
        override fun areContentsTheSame(oldItem: ConnectionDto, newItem: ConnectionDto) = oldItem == newItem
    }
}
