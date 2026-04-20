package com.studenthousing.app.ui.roommates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studenthousing.app.R
import com.studenthousing.app.data.model.RoommateRequestDto
import com.studenthousing.app.databinding.ItemBookingRequestBinding

class RequestAdapter(
    private val onAccept: (RoommateRequestDto) -> Unit,
    private val onReject: (RoommateRequestDto) -> Unit
) : ListAdapter<RoommateRequestDto, RequestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onAccept, onReject)
    }

    class ViewHolder(private val binding: ItemBookingRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RoommateRequestDto, onAccept: (RoommateRequestDto) -> Unit, onReject: (RoommateRequestDto) -> Unit) {
            val from = item.fromStudent
            binding.propertyTitle.text = "${from?.firstName ?: ""} ${from?.lastName ?: ""}".trim().ifBlank { "Student" }
            binding.studentName.text = "Wants to be your roommate"
            binding.priceText.visibility = android.view.View.GONE
            binding.statusChip.text = "Pending"
            binding.statusChip.setTextColor(binding.root.context.getColor(R.color.warning_orange))
            binding.actionsLayout.visibility = android.view.View.VISIBLE
            binding.btnConfirm.text = binding.root.context.getString(R.string.accept)
            binding.btnReject.text = binding.root.context.getString(R.string.decline)
            binding.btnConfirm.setOnClickListener { onAccept(item) }
            binding.btnReject.setOnClickListener { onReject(item) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RoommateRequestDto>() {
        override fun areItemsTheSame(o: RoommateRequestDto, n: RoommateRequestDto) = o.connectionId == n.connectionId
        override fun areContentsTheSame(o: RoommateRequestDto, n: RoommateRequestDto) = o == n
    }
}
