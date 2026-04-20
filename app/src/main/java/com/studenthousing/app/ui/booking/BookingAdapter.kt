package com.studenthousing.app.ui.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studenthousing.app.R
import com.studenthousing.app.data.local.BookingEntity
import com.studenthousing.app.databinding.ItemBookingBinding

class BookingAdapter(
    private val onCancelClick: ((BookingEntity) -> Unit)? = null
) : ListAdapter<BookingEntity, BookingAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position), onCancelClick)
    }

    class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BookingEntity, onCancelClick: ((BookingEntity) -> Unit)?) {
            binding.bookingPropertyTitle.text = item.propertyTitle ?: "Unknown property"
            binding.bookingPrice.text = "$${item.finalPrice ?: 0.0}/mo"

            val status = item.status.lowercase()
            binding.bookingStatus.text = item.status.replaceFirstChar { it.uppercase() }
            val statusColor = when {
                status.contains("confirm") -> R.color.success
                status.contains("cancel") -> R.color.error
                else -> R.color.warning_orange
            }
            binding.bookingStatus.setTextColor(binding.root.context.getColor(statusColor))

            // Long press to cancel (only for pending/confirmed)
            val canCancel = status == "pending" || status == "confirmed"
            if (canCancel && onCancelClick != null) {
                binding.root.setOnLongClickListener {
                    onCancelClick(item)
                    true
                }
            } else {
                binding.root.setOnLongClickListener(null)
            }
        }
    }

    private class BookingDiffCallback : DiffUtil.ItemCallback<BookingEntity>() {
        override fun areItemsTheSame(oldItem: BookingEntity, newItem: BookingEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookingEntity, newItem: BookingEntity): Boolean {
            return oldItem == newItem
        }
    }
}
