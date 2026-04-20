package com.studenthousing.app.ui.owner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studenthousing.app.R
import com.studenthousing.app.data.model.BookingDto
import com.studenthousing.app.databinding.ItemBookingRequestBinding

class BookingRequestAdapter(
    private val onConfirm: (BookingDto) -> Unit,
    private val onReject: (BookingDto) -> Unit
) : ListAdapter<BookingDto, BookingRequestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onConfirm, onReject)
    }

    class ViewHolder(private val binding: ItemBookingRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BookingDto, onConfirm: (BookingDto) -> Unit, onReject: (BookingDto) -> Unit) {
            binding.propertyTitle.text = item.property?.title ?: "Unknown property"
            binding.priceText.text = "$${item.finalPrice ?: 0.0}/mo"

            val studentName = item.student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown student"
            binding.studentName.text = "Student: $studentName"

            val status = item.status.lowercase()
            binding.statusChip.text = item.status.replaceFirstChar { it.uppercase() }
            val ctx = binding.root.context
            val statusColor = when {
                status.contains("confirm") -> R.color.success
                status.contains("cancel") -> R.color.error
                else -> R.color.warning_orange
            }
            binding.statusChip.setTextColor(ctx.getColor(statusColor))

            // Show confirm/reject buttons only for pending bookings
            if (status == "pending") {
                binding.actionsLayout.visibility = View.VISIBLE
                binding.btnConfirm.setOnClickListener { onConfirm(item) }
                binding.btnReject.setOnClickListener { onReject(item) }
            } else {
                binding.actionsLayout.visibility = View.GONE
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<BookingDto>() {
        override fun areItemsTheSame(oldItem: BookingDto, newItem: BookingDto) = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: BookingDto, newItem: BookingDto) = oldItem == newItem
    }
}
