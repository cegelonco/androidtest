package com.studenthousing.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.model.CampusData
import com.studenthousing.app.data.model.RegisterRequest
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentSignupBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class SignupFragment : Fragment(R.layout.fragment_signup) {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SignupViewModel

    private var selectedUserType = "student"
    private var selectedUniversity: String = ""
    private var selectedUsjCampus: String = ""
    private var selectedMajor: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignupBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(
            this,
            CommonViewModelFactory(app.container.repository)
        )[SignupViewModel::class.java]

        // Toggle between student and owner
        updateFieldsVisibility()
        binding.userTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedUserType = if (checkedId == R.id.btnStudent) "student" else "owner"
                updateFieldsVisibility()
            }
        }

        setupUniversitySpinner()
        setupUsjCampusSpinner()
        setupMajorSpinner()

        binding.registerButton.setOnClickListener {
            clearErrors()
            val firstName = binding.firstNameInput.text?.toString().orEmpty().trim()
            val lastName = binding.lastNameInput.text?.toString().orEmpty().trim()
            val email = binding.signupEmailInput.text?.toString().orEmpty().trim()
            val phone = binding.phoneInput.text?.toString().orEmpty().trim()
            val password = binding.signupPasswordInput.text?.toString().orEmpty()
            val confirmPassword = binding.confirmPasswordInput.text?.toString().orEmpty()

            // Build final university string: "USJ - ESIB / CST" if USJ with campus, else just name
            val universityFinal = when {
                selectedUniversity.isBlank() -> ""
                selectedUniversity == "USJ" && selectedUsjCampus.isNotBlank() ->
                    "USJ - $selectedUsjCampus"
                else -> selectedUniversity
            }

            if (!validate(firstName, lastName, email, phone, password, confirmPassword, universityFinal)) {
                return@setOnClickListener
            }

            viewModel.register(
                RegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    phoneCode = "+961",
                    password = password,
                    university = if (selectedUserType == "student") universityFinal else null,
                    department = if (selectedUserType == "student" && selectedMajor.isNotBlank()) selectedMajor else null,
                    budget = null,
                    userType = selectedUserType
                )
            )
        }

        binding.loginLink.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.signupProgress.visibility = View.VISIBLE
                    binding.registerButton.isEnabled = false
                    binding.signupErrorText.text = ""
                }
                is ResultState.Success -> {
                    binding.signupProgress.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    val (email, code) = state.data
                    val bundle = Bundle().apply {
                        putString("email", email)
                        if (code != null) putString("code", code)
                    }
                    findNavController().navigate(R.id.action_signup_to_verify, bundle)
                }
                is ResultState.Error -> {
                    binding.signupProgress.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    binding.signupErrorText.text = state.message
                }
            }
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
                    selectedUniversity = if (selected == "Select University") "" else selected

                    val showUsj = selected == "USJ"
                    binding.usjCampusLabel.visibility = if (showUsj) View.VISIBLE else View.GONE
                    binding.usjCampusSpinner.visibility = if (showUsj) View.VISIBLE else View.GONE

                    selectedUsjCampus = if (showUsj) CampusData.usjCampuses[0].name else ""
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                    selectedUsjCampus = CampusData.usjCampuses[position].name
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupMajorSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            CampusData.majors
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.majorSpinner.adapter = adapter

        binding.majorSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selected = CampusData.majors[position]
                    selectedMajor = if (selected == "Select Major") "" else selected
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun updateFieldsVisibility() {
        val isStudent = selectedUserType == "student"
        binding.universityLayout.visibility = if (isStudent) View.VISIBLE else View.GONE
        binding.signupSubtitle.text = if (isStudent) {
            getString(R.string.signup_student_subtitle)
        } else {
            getString(R.string.signup_owner_subtitle)
        }
    }

    private fun validate(
        firstName: String, lastName: String, email: String,
        phone: String, password: String, confirmPassword: String,
        university: String
    ): Boolean {
        if (firstName.isBlank()) {
            binding.firstNameLayout.error = getString(R.string.field_required)
            return false
        }
        if (lastName.isBlank()) {
            binding.lastNameLayout.error = getString(R.string.field_required)
            return false
        }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signupEmailLayout.error = getString(R.string.error_invalid_email)
            return false
        }
        if (phone.isBlank() || phone.length < 7) {
            binding.phoneLayout.error = getString(R.string.error_invalid_phone)
            return false
        }
        if (password.length < 6) {
            binding.signupPasswordLayout.error = getString(R.string.error_password_short)
            return false
        }
        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.error_passwords_mismatch)
            return false
        }
        if (selectedUserType == "student" && university.isBlank()) {
            binding.signupErrorText.text = "Please select your university"
            return false
        }
        return true
    }

    private fun clearErrors() {
        binding.firstNameLayout.error = null
        binding.lastNameLayout.error = null
        binding.signupEmailLayout.error = null
        binding.phoneLayout.error = null
        binding.signupPasswordLayout.error = null
        binding.confirmPasswordLayout.error = null
        binding.signupErrorText.text = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
