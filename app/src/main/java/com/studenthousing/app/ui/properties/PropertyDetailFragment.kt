package com.studenthousing.app.ui.properties

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentPropertyDetailBinding
import com.studenthousing.app.ui.CommonViewModelFactory
import com.studenthousing.app.ui.booking.BookingsViewModel
import kotlinx.coroutines.launch

class PropertyDetailFragment : Fragment(R.layout.fragment_property_detail) {
    private var _binding: FragmentPropertyDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var detailViewModel: PropertyDetailViewModel
    private lateinit var bookingsViewModel: BookingsViewModel
    private var propertyId: String = ""
    private var isFavorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPropertyDetailBinding.bind(view)

        propertyId = arguments?.getString("property_id").orEmpty()
        val app = requireContext().applicationContext as StudentHousingApp
        val factory = CommonViewModelFactory(app.container.repository)
        detailViewModel = ViewModelProvider(this, factory)[PropertyDetailViewModel::class.java]
        bookingsViewModel = ViewModelProvider(this, factory)[BookingsViewModel::class.java]

        // Show Book Now only for students
        val isOwner = app.container.tokenStore.cachedUserType == "owner"
        binding.bookNowButton.visibility = if (isOwner) View.GONE else View.VISIBLE

        // Check favorite status (students only)
        if (!isOwner && propertyId.isNotBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                isFavorite = app.container.repository.checkFavorite(propertyId)
                updateFavoriteIcon()
            }
        }
        binding.favoriteButton.visibility = if (isOwner) View.GONE else View.VISIBLE

        binding.favoriteButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val result = app.container.repository.toggleFavorite(propertyId, isFavorite)
                if (result is ResultState.Success) {
                    isFavorite = result.data
                    updateFavoriteIcon()
                }
            }
        }

        detailViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Success -> {
                    val property = state.data
                    binding.detailTitle.text = property.title
                    binding.detailAddress.text = property.address
                    binding.detailPrice.text = "$${property.price}/mo"
                    binding.detailType.text = property.type ?: "Property"
                    binding.detailBody.text = property.description
                        ?: getString(R.string.no_description)

                    if (!property.imageUrl.isNullOrBlank()) {
                        binding.detailImage.load(property.imageUrl) {
                            placeholder(R.drawable.placeholder_property)
                            error(R.drawable.placeholder_property)
                            crossfade(true)
                        }
                    }

                    // Show map if property has coordinates
                    if (property.latitude != null && property.longitude != null) {
                        binding.locationLabel.visibility = View.VISIBLE
                        binding.detailMapView.visibility = View.VISIBLE
                        val mapFragment = childFragmentManager
                            .findFragmentById(R.id.detailMapView) as? SupportMapFragment
                        mapFragment?.getMapAsync { map ->
                            val propertyLatLng = LatLng(property.latitude, property.longitude)
                            map.uiSettings.isZoomControlsEnabled = false
                            map.uiSettings.isScrollGesturesEnabled = false
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(propertyLatLng, 15f))
                            map.addMarker(
                                MarkerOptions()
                                    .position(propertyLatLng)
                                    .title(property.title)
                            )
                        }
                    }
                }
                is ResultState.Error -> {
                    binding.detailBody.text = state.message
                }
                ResultState.Loading -> {
                    binding.detailBody.text = "Loading..."
                }
            }
        }

        binding.bookNowButton.setOnClickListener {
            if (propertyId.isBlank()) return@setOnClickListener
            binding.bookNowButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                val result = app.container.repository.createBooking(propertyId)
                binding.bookNowButton.isEnabled = true
                when (result) {
                    is ResultState.Success -> {
                        Snackbar.make(binding.root, "Booking request sent!", Snackbar.LENGTH_SHORT).show()
                    }
                    is ResultState.Error -> {
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        if (propertyId.isNotBlank()) detailViewModel.load(propertyId)
    }

    private fun updateFavoriteIcon() {
        binding.favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
