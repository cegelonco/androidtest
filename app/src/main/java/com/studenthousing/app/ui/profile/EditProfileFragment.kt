package com.studenthousing.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.model.CampusData
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentEditProfileBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditProfileViewModel

    private var selectedUniversity: String = ""
    private var selectedUsjCampus: String = ""
    private var selectedMajor: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(
            this,
            CommonViewModelFactory(app.container.repository)
        )[EditProfileViewModel::class.java]

        // Pre-fill fields from arguments if passed
        val currentPhone = arguments?.getString("phone") ?: ""
        val currentUniversity = arguments?.getString("university") ?: ""
        val currentMajor = arguments?.getString("major") ?: ""

        binding.editPhoneInput.setText(currentPhone)

        setupUniversitySpinner(currentUniversity)
        setupUsjSpinner()
        setupMajorSpinner(currentMajor)

        binding.btnSaveProfile.setOnClickListener {
            val phone = binding.editPhoneInput.text?.toString().orEmpty().trim()

            val universityFinal = when {
                selectedUniversity.isBlank() -> currentUniversity
                selectedUniversity == "USJ" && selectedUsjCampus.isNotBlank() ->
                    "USJ - $selectedUsjCampus"
                else -> selectedUniversity
            }

            val majorFinal = selectedMajor.ifBlank { currentMajor }

            viewModel.updateProfile(phone, universityFinal, majorFinal)
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.editProfileProgress.visibility = View.VISIBLE
                    binding.btnSaveProfile.isEnabled = false
                    binding.editProfileError.visibility = View.GONE
                }
                is ResultState.Success -> {
                    binding.editProfileProgress.visibility = View.GONE
                    binding.btnSaveProfile.isEnabled = true
                    // Go back to profile screen
                    findNavController().popBackStack()
                }
                is ResultState.Error -> {
                    binding.editProfileProgress.visibility = View.GONE
                    binding.btnSaveProfile.isEnabled = true
                    binding.editProfileError.visibility = View.VISIBLE
                    binding.editProfileError.text = state.message
                }
            }
        }
    }

    private fun setupUniversitySpinner(current: String) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            CampusData.universities
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.editUniversitySpinner.adapter = adapter

        // Pre-select current university
        val index = CampusData.universities.indexOf(
            CampusData.universities.firstOrNull { current.startsWith(it) } ?: "Select University"
        )
        if (index >= 0) binding.editUniversitySpinner.setSelection(index)

        binding.editUniversitySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selected = CampusData.universities[position]
                    selectedUniversity = if (selected == "Select University") "" else selected
                    val showUsj = selected == "USJ"
                    binding.editUsjLabel.visibility = if (showUsj) View.VISIBLE else View.GONE
                    binding.editUsjSpinner.visibility = if (showUsj) View.VISIBLE else View.GONE
                    if (showUsj) selectedUsjCampus = CampusData.usjCampuses[0].name
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupUsjSpinner() {
        val names = CampusData.usjCampuses.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.editUsjSpinner.adapter = adapter

        binding.editUsjSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedUsjCampus = CampusData.usjCampuses[position].name
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupMajorSpinner(current: String) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            CampusData.majors
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.editMajorSpinner.adapter = adapter

        // Pre-select current major
        val index = CampusData.majors.indexOf(current).takeIf { it >= 0 } ?: 0
        binding.editMajorSpinner.setSelection(index)

        binding.editMajorSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selected = CampusData.majors[position]
                    selectedMajor = if (selected == "Select Major") "" else selected
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
