package com.studenthousing.app.ui.owner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.studenthousing.app.R
import com.studenthousing.app.databinding.FragmentMapPickerBinding

class MapPickerFragment : Fragment(R.layout.fragment_map_picker) {
    private var _binding: FragmentMapPickerBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var selectedLatLng: LatLng? = null

    // Default: Beirut, Lebanon
    private val defaultLocation = LatLng(33.8938, 35.5018)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enableMyLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapPickerBinding.bind(view)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map

            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            // Always start on Beirut, Lebanon
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f))

            // Request location permission to show blue dot
            val hasPermission = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                enableMyLocation()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            // Tap to place marker
            map.setOnMapClickListener { latLng ->
                selectedLatLng = latLng
                map.clear()
                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Property Location")
                )
                binding.selectedLocationText.text = getString(
                    R.string.location_selected_format,
                    latLng.latitude,
                    latLng.longitude
                )
                binding.selectedLocationText.setTextColor(
                    requireContext().getColor(R.color.success)
                )
            }
        }

        binding.confirmLocationButton.setOnClickListener {
            val latLng = selectedLatLng
            if (latLng != null) {
                setFragmentResult(
                    LOCATION_REQUEST_KEY,
                    bundleOf(
                        KEY_LAT to latLng.latitude,
                        KEY_LNG to latLng.longitude
                    )
                )
                findNavController().popBackStack()
            } else {
                binding.selectedLocationText.text = getString(R.string.please_select_location)
                binding.selectedLocationText.setTextColor(
                    requireContext().getColor(R.color.error)
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        googleMap?.isMyLocationEnabled = true

        // Only move to GPS location if it's in the Lebanon/Middle East region
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                // Lebanon region: lat 33-35, lng 35-37
                if (lat in 33.0..35.0 && lng in 34.5..37.0) {
                    val myLatLng = LatLng(lat, lng)
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val LOCATION_REQUEST_KEY = "map_location_result"
        const val KEY_LAT = "latitude"
        const val KEY_LNG = "longitude"
    }
}
