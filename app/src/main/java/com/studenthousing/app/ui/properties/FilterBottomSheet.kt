package com.studenthousing.app.ui.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.studenthousing.app.R
import com.studenthousing.app.data.model.CampusData
import com.studenthousing.app.databinding.BottomSheetFilterBinding

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    private var selectedCampusLat: Double? = null
    private var selectedCampusLng: Double? = null
    private var selectedCampusName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPriceSlider()
        setupUniversitySpinner()
        setupUsjCampusSpinner()
        setupDistanceSlider()
        setupButtons()
    }

    private fun setupPriceSlider() {
        binding.priceSlider.addOnChangeListener { _, value, _ ->
            binding.priceLabel.text =
                if (value >= 5000f) getString(R.string.any_price) else "$${value.toInt()}"
        }
    }

    private fun setupUniversitySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            CampusData.universities
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.universitySpinner.adapter = adapter

        binding.universitySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selected = CampusData.universities[position]

                    when {
                        selected == "Select University" -> {
                            selectedCampusLat = null
                            selectedCampusLng = null
                            selectedCampusName = null
                            binding.labelSubCampus.visibility = View.GONE
                            binding.usjCampusSpinner.visibility = View.GONE
                            binding.distanceSection.visibility = View.GONE
                        }

                        selected == "USJ" -> {
                            binding.labelSubCampus.visibility = View.VISIBLE
                            binding.usjCampusSpinner.visibility = View.VISIBLE
                            binding.distanceSection.visibility = View.VISIBLE

                            val first = CampusData.usjCampuses.firstOrNull()
                            selectedCampusLat = first?.lat
                            selectedCampusLng = first?.lng
                            selectedCampusName = first?.name
                        }

                        else -> {
                            val campus = CampusData.getCampus(selected)
                            selectedCampusLat = campus?.lat
                            selectedCampusLng = campus?.lng
                            selectedCampusName = campus?.name
                            binding.labelSubCampus.visibility = View.GONE
                            binding.usjCampusSpinner.visibility = View.GONE
                            binding.distanceSection.visibility =
                                if (campus != null) View.VISIBLE else View.GONE
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun setupUsjCampusSpinner() {
        val names = CampusData.usjCampuses.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.usjCampusSpinner.adapter = adapter

        binding.usjCampusSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val campus = CampusData.usjCampuses[position]
                    selectedCampusLat = campus.lat
                    selectedCampusLng = campus.lng
                    selectedCampusName = campus.name
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun setupDistanceSlider() {
        binding.distanceSlider.addOnChangeListener { _, value, _ ->
            binding.distanceLabel.text = "${value.toInt()} km"
        }
    }

    private fun setupButtons() {
        binding.btnApply.setOnClickListener {
            val type = when (binding.filterTypeToggle.checkedButtonId) {
                R.id.filterApartment -> "apartment"
                R.id.filterHouse -> "house"
                R.id.filterStudio -> "studio"
                R.id.filterDorm -> "dorm"
                else -> ""
            }

            val minRooms = when (binding.filterRoomsGroup.checkedChipId) {
                R.id.rooms1 -> 1
                R.id.rooms2 -> 2
                R.id.rooms3plus -> 3
                else -> 0
            }

            val maxPrice = binding.priceSlider.value
            val priceValue = if (maxPrice >= 5000f) 0.0 else maxPrice.toDouble()
            val maxDistance = binding.distanceSlider.value.toDouble()

            setFragmentResult(
                FILTER_REQUEST_KEY,
                bundleOf(
                    KEY_TYPE to type,
                    KEY_MIN_ROOMS to minRooms,
                    KEY_MAX_PRICE to priceValue,
                    KEY_CAMPUS_LAT to (selectedCampusLat ?: 0.0),
                    KEY_CAMPUS_LNG to (selectedCampusLng ?: 0.0),
                    KEY_CAMPUS_NAME to (selectedCampusName ?: "campus"),
                    KEY_MAX_DIST_KM to maxDistance
                )
            )
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            selectedCampusLat = null
            selectedCampusLng = null
            selectedCampusName = null

            setFragmentResult(
                FILTER_REQUEST_KEY,
                bundleOf(
                    KEY_TYPE to "",
                    KEY_MIN_ROOMS to 0,
                    KEY_MAX_PRICE to 0.0,
                    KEY_CAMPUS_LAT to 0.0,
                    KEY_CAMPUS_LNG to 0.0,
                    KEY_CAMPUS_NAME to "campus",
                    KEY_MAX_DIST_KM to 0.0
                )
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FILTER_REQUEST_KEY = "filter_result"
        const val KEY_TYPE = "type"
        const val KEY_MIN_ROOMS = "min_rooms"
        const val KEY_MAX_PRICE = "max_price"
        const val KEY_CAMPUS_LAT = "campus_lat"
        const val KEY_CAMPUS_LNG = "campus_lng"
        const val KEY_CAMPUS_NAME = "campus_name"
        const val KEY_MAX_DIST_KM = "max_dist_km"
    }
}
