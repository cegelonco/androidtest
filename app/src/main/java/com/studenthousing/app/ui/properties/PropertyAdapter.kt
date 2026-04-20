package com.studenthousing.app.ui.properties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.studenthousing.app.BuildConfig
import com.studenthousing.app.R
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.databinding.ItemPropertyBinding
import com.studenthousing.app.util.LocationHelper

class PropertyAdapter(
    private val onClick: (PropertyEntity) -> Unit
) : ListAdapter<PropertyEntity, PropertyAdapter.PropertyViewHolder>(PropertyDiffCallback()) {

    // Campus-based distance (set via filter)
    private var campusLat: Double? = null
    private var campusLng: Double? = null
    private var campusName: String = "campus"

    // User-based distance (set when Near Me is enabled)
    private var userLat: Double? = null
    private var userLng: Double? = null

    fun setCampus(lat: Double, lng: Double, name: String?) {
        campusLat = lat
        campusLng = lng
        campusName = name?.takeIf { it.isNotBlank() } ?: "campus"
        notifyDataSetChanged()
    }

    fun setUserLocation(lat: Double?, lng: Double?) {
        userLat = lat
        userLng = lng
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, userLat, userLng, campusLat, campusLng, campusName)
    }

    class PropertyViewHolder(private val binding: ItemPropertyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: PropertyEntity,
            onClick: (PropertyEntity) -> Unit,
            userLat: Double?,
            userLng: Double?,
            campusLat: Double?,
            campusLng: Double?,
            campusName: String
        ) {
            binding.titleText.text = item.title
            binding.addressText.text = item.address
            binding.priceText.text = "$${item.price}/mo"

            if (!item.imageUrl.isNullOrBlank()) {
                binding.propertyImage.load(item.imageUrl) {
                    placeholder(R.drawable.placeholder_property)
                    error(R.drawable.placeholder_property)
                    crossfade(true)
                }
            } else {
                binding.propertyImage.setImageResource(R.drawable.placeholder_property)
            }

            if (!item.type.isNullOrBlank()) {
                binding.typeChip.text = item.type
                binding.typeChip.visibility = View.VISIBLE
            } else {
                binding.typeChip.visibility = View.GONE
            }

            // Priority: user location (Near Me) > campus (filter) > hidden
            when {
                userLat != null && userLng != null &&
                    item.latitude != null && item.longitude != null -> {
                    val km = LocationHelper.distanceKm(
                        item.latitude,
                        item.longitude,
                        userLat,
                        userLng
                    )
                    binding.distanceText.text = String.format("%.1f km from you", km)
                    binding.distanceText.visibility = View.VISIBLE
                }
                campusLat != null && campusLng != null &&
                    item.latitude != null && item.longitude != null -> {
                    val km = LocationHelper.distanceKm(
                        item.latitude,
                        item.longitude,
                        campusLat,
                        campusLng
                    )
                    binding.distanceText.text = String.format("%.1f km from %s", km, campusName)
                    binding.distanceText.visibility = View.VISIBLE
                }
                else -> {
                    binding.distanceText.visibility = View.GONE
                }
            }

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private class PropertyDiffCallback : DiffUtil.ItemCallback<PropertyEntity>() {
        override fun areItemsTheSame(oldItem: PropertyEntity, newItem: PropertyEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PropertyEntity, newItem: PropertyEntity): Boolean {
            return oldItem == newItem
        }
    }
}
