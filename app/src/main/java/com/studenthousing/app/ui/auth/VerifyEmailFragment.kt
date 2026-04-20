package com.studenthousing.app.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentVerifyEmailBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class VerifyEmailFragment : Fragment(R.layout.fragment_verify_email) {
    private var _binding: FragmentVerifyEmailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: VerifyEmailViewModel
    private var email: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerifyEmailBinding.bind(view)

        email = arguments?.getString("email").orEmpty()
        val code = arguments?.getString("code")

        // If email was sent, tell user to check email. If code returned, auto-fill it.
        if (code.isNullOrBlank()) {
            binding.verifySubtitle.text = getString(R.string.verification_sent_to, email)
        } else {
            binding.verifySubtitle.text = getString(R.string.verification_sent_to, email)
            binding.codeInput.setText(code)
        }

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[VerifyEmailViewModel::class.java]

        binding.verifyButton.setOnClickListener {
            val enteredCode = binding.codeInput.text?.toString().orEmpty().trim()
            if (enteredCode.length != 6) {
                binding.codeLayout.error = getString(R.string.error_invalid_code)
                return@setOnClickListener
            }
            binding.codeLayout.error = null
            viewModel.verify(email, enteredCode)
        }

        binding.resendText.setOnClickListener {
            viewModel.resend(email)
        }

        viewModel.verifyState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.verifyProgress.visibility = View.VISIBLE
                    binding.verifyButton.isEnabled = false
                    binding.verifyErrorText.text = ""
                }
                is ResultState.Success -> {
                    binding.verifyProgress.visibility = View.GONE
                    binding.verifyButton.isEnabled = true
                    findNavController().navigate(R.id.action_verify_to_properties)
                }
                is ResultState.Error -> {
                    binding.verifyProgress.visibility = View.GONE
                    binding.verifyButton.isEnabled = true
                    binding.verifyErrorText.text = state.message
                }
            }
        }

        viewModel.resendState.observe(viewLifecycleOwner) { state ->
            if (state is ResultState.Success) {
                val newCode = state.data
                if (!newCode.isNullOrBlank()) {
                    binding.codeInput.setText(newCode)
                }
                Snackbar.make(binding.root, getString(R.string.code_resent), Snackbar.LENGTH_SHORT).show()
            } else if (state is ResultState.Error) {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.resendCooldown.observe(viewLifecycleOwner) { seconds ->
            if (seconds > 0) {
                binding.resendText.text = getString(R.string.resend_in, seconds)
                binding.resendText.isEnabled = false
            } else {
                binding.resendText.text = getString(R.string.resend_code)
                binding.resendText.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
