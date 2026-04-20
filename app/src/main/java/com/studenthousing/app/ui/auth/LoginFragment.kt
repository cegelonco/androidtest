package com.studenthousing.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentLoginBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[LoginViewModel::class.java]

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString().orEmpty().trim()
            val password = binding.passwordInput.text?.toString().orEmpty()

            // Validate inputs
            binding.emailLayout.error = null
            binding.passwordLayout.error = null

            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailLayout.error = getString(R.string.error_invalid_email)
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.passwordLayout.error = getString(R.string.error_password_short)
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.signupLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.loginProgress.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                    binding.loginErrorText.text = ""
                }
                is ResultState.Success -> {
                    binding.loginProgress.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    findNavController().navigate(R.id.action_login_to_properties)
                }
                is ResultState.Error -> {
                    binding.loginProgress.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    binding.loginErrorText.text = state.message
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
