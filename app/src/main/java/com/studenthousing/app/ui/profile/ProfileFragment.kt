package com.studenthousing.app.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentProfileBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[ProfileViewModel::class.java]

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.profileProgress.visibility = View.VISIBLE
                    binding.profileError.text = ""
                }
                is ResultState.Success -> {
                    binding.profileProgress.visibility = View.GONE
                    val user = state.data
                    binding.profileName.text = "${user.firstName} ${user.lastName}"
                    binding.profileEmail.text = user.email
                    binding.profileType.text = user.userType.replaceFirstChar { it.uppercase() }

                    binding.profilePhone.text = if (!user.phone.isNullOrBlank()) {
                        "Phone: ${user.phoneCode ?: ""}${user.phone}"
                    } else "Phone: Not set"

                    // Edit profile button — passes current values to edit screen
                    binding.btnEditProfile.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("phone", user.phone ?: "")
                            putString("university", user.university ?: "")
                            putString("major", user.department ?: "")
                        }
                        findNavController().navigate(R.id.action_profile_to_editProfile, bundle)
                    }

                    if (user.userType == "student") {
                        binding.profileUniversity.text = "University: ${user.university ?: "Not set"}"
                        binding.profileDepartment.text = "Major: ${user.department ?: "Not set"}"
                        // Hide budget — no longer displayed
                        binding.profileExtra.visibility = View.GONE

                        // Student: show Favorites + Bookings
                        binding.btnFavorites.visibility = View.VISIBLE
                        binding.btnFavorites.setOnClickListener {
                            findNavController().navigate(R.id.favoritesFragment)
                        }
                        binding.btnBookings.text = getString(R.string.my_bookings)
                        binding.btnBookings.setOnClickListener {
                            findNavController().navigate(R.id.bookingsFragment)
                        }
                        binding.btnRoommates.visibility = View.VISIBLE
                        binding.btnRoommates.setOnClickListener {
                            findNavController().navigate(R.id.roommatesFragment)
                        }
                    } else {
                        binding.profileUniversity.text = "Properties: ${user.totalProperties ?: 0}"
                        binding.profileDepartment.text = "Rating: ${user.rating ?: 0.0}/5"
                        binding.profileExtra.visibility = View.VISIBLE
                        binding.profileExtra.text = "Response time: ${user.responseTime ?: "N/A"}"

                        // Owner: show My Properties + Booking Requests, hide Favorites
                        binding.btnFavorites.visibility = View.GONE
                        binding.btnBookings.text = getString(R.string.my_properties)
                        binding.btnBookings.setOnClickListener {
                            findNavController().navigate(R.id.myPropertiesFragment)
                        }
                        binding.btnBookingRequests.visibility = View.VISIBLE
                        binding.btnBookingRequests.setOnClickListener {
                            findNavController().navigate(R.id.bookingRequestsFragment)
                        }
                    }
                }
                is ResultState.Error -> {
                    binding.profileProgress.visibility = View.GONE
                    binding.profileError.text = state.message
                }
            }
        }

        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
