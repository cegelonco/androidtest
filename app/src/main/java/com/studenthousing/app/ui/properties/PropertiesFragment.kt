package com.studenthousing.app.ui.properties

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentPropertiesBinding
import com.studenthousing.app.ui.CommonViewModelFactory
import com.studenthousing.app.util.NetworkUtils
import com.studenthousing.app.util.ShakeDetector

class PropertiesFragment : Fragment(R.layout.fragment_properties) {
    private var _binding: FragmentPropertiesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PropertiesViewModel
    private lateinit var adapter: PropertyAdapter
    private var shakeDetector: ShakeDetector? = null
    private var locationCancellationSource: CancellationTokenSource? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchUserLocationAndSort()
        } else {
            binding.nearCampusSwitch.isChecked = false
            Snackbar.make(binding.root, "Location permission required for this feature", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPropertiesBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[PropertiesViewModel::class.java]

        adapter = PropertyAdapter { property ->
            findNavController().navigate(
                R.id.action_properties_to_detail,
                Bundle().apply { putString("property_id", property.id) }
            )
        }
        binding.propertiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.propertiesRecycler.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { loadProperties() }

        shakeDetector = ShakeDetector(requireContext()) {
            if (!isAdded || _binding == null) return@ShakeDetector
            if (binding.swipeRefresh.isRefreshing) return@ShakeDetector

            binding.swipeRefresh.isRefreshing = true
            Toast.makeText(requireContext(), "Refreshing properties...", Toast.LENGTH_SHORT).show()
            loadProperties()
        }

        // Show different button for owner vs student
        val isOwner = app.container.tokenStore.cachedUserType == "owner"
        if (isOwner) {
            binding.bookingsButton.text = getString(R.string.add_property)
            binding.bookingsButton.setOnClickListener {
                findNavController().navigate(R.id.addPropertyFragment)
            }
        } else {
            binding.bookingsButton.setOnClickListener {
                findNavController().navigate(R.id.action_properties_to_bookings)
            }
        }

        // Filter button
        binding.filterButton.setOnClickListener {
            FilterBottomSheet().show(childFragmentManager, "filter")
        }

        // Sort chips
        binding.sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val list = when {
                checkedIds.contains(R.id.sortPriceLow) -> viewModel.sortByPriceLowToHigh()
                checkedIds.contains(R.id.sortPriceHigh) -> viewModel.sortByPriceHighToLow()
                else -> viewModel.getDefault()
            }
            adapter.submitList(list)
            binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // Listen for filter results
        childFragmentManager.setFragmentResultListener(
            FilterBottomSheet.FILTER_REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val type = bundle.getString(FilterBottomSheet.KEY_TYPE, "")
            val minRooms = bundle.getInt(FilterBottomSheet.KEY_MIN_ROOMS, 0)
            val maxPrice = bundle.getDouble(FilterBottomSheet.KEY_MAX_PRICE, 0.0)
            val campusLat = bundle.getDouble(FilterBottomSheet.KEY_CAMPUS_LAT, 0.0)
            val campusLng = bundle.getDouble(FilterBottomSheet.KEY_CAMPUS_LNG, 0.0)
            val campusName = bundle.getString(FilterBottomSheet.KEY_CAMPUS_NAME, "campus")
            val maxDist = bundle.getDouble(FilterBottomSheet.KEY_MAX_DIST_KM, 0.0)

            // Update adapter with selected campus so distances show from that campus
            if (campusLat != 0.0 && campusLng != 0.0) {
                adapter.setCampus(campusLat, campusLng, campusName)
            }

            viewModel.applyFilters(
                type = type.ifBlank { null },
                minRooms = if (minRooms > 0) minRooms else null,
                maxPrice = if (maxPrice > 0.0) maxPrice else null,
                campusLat = if (campusLat != 0.0) campusLat else null,
                campusLng = if (campusLng != 0.0) campusLng else null,
                maxDistanceKm = if (maxDist > 0.0) maxDist else null
            )
        }

        binding.nearCampusSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    fetchUserLocationAndSort()
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                adapter.setUserLocation(null, null)
                adapter.submitList(viewModel.sortByDistanceToCampus(false))
            }
        }

        // Search with debounce
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s?.toString().orEmpty())
            }
        })

        viewModel.offline.observe(viewLifecycleOwner) { offline ->
            binding.offlineBanner.visibility = if (offline) View.VISIBLE else View.GONE
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is ResultState.Loading -> binding.propertiesProgress.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.propertiesProgress.visibility = View.GONE
                    val list = if (binding.nearCampusSwitch.isChecked) {
                        viewModel.sortByDistanceToCampus(true)
                    } else state.data
                    adapter.submitList(list)
                    binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
                is ResultState.Error -> {
                    binding.propertiesProgress.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        loadProperties()
    }

    @SuppressLint("MissingPermission")
    private fun fetchUserLocationAndSort() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                applyLocation(location.latitude, location.longitude)
            } else {
                // No cached location — request a fresh fix
                val cts = CancellationTokenSource()
                locationCancellationSource = cts
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { freshLocation ->
                        if (freshLocation != null) {
                            applyLocation(freshLocation.latitude, freshLocation.longitude)
                        } else {
                            Snackbar.make(binding.root, "Using default location (GPS unavailable)", Snackbar.LENGTH_SHORT).show()
                            adapter.submitList(viewModel.sortByDistanceToCampus(true))
                        }
                    }
                    .addOnFailureListener {
                        Snackbar.make(binding.root, "Could not get location, using default", Snackbar.LENGTH_SHORT).show()
                        adapter.submitList(viewModel.sortByDistanceToCampus(true))
                    }
            }
        }.addOnFailureListener {
            Snackbar.make(binding.root, "Could not get location, using default", Snackbar.LENGTH_SHORT).show()
            adapter.submitList(viewModel.sortByDistanceToCampus(true))
        }
    }

    private fun applyLocation(lat: Double, lng: Double) {
        viewModel.setUserLocation(lat, lng)
        adapter.setUserLocation(lat, lng)
        Snackbar.make(binding.root, "Sorted by distance from your location", Snackbar.LENGTH_SHORT).show()
        adapter.submitList(viewModel.sortByDistanceToCampus(true))
    }

    private fun loadProperties() {
        val online = NetworkUtils.isOnline(requireContext())
        viewModel.load(online)
    }

    override fun onResume() {
        super.onResume()
        shakeDetector?.start()
    }

    override fun onPause() {
        shakeDetector?.stop()
        super.onPause()
    }

    override fun onDestroyView() {
        shakeDetector?.stop()
        locationCancellationSource?.cancel()
        locationCancellationSource = null
        _binding = null
        super.onDestroyView()
    }
}
