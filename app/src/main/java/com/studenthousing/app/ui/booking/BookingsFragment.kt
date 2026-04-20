package com.studenthousing.app.ui.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentBookingsBinding
import com.studenthousing.app.ui.CommonViewModelFactory
import com.studenthousing.app.util.NetworkUtils

class BookingsFragment : Fragment(R.layout.fragment_bookings) {
    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookingsViewModel
    private lateinit var adapter: BookingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingsBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[BookingsViewModel::class.java]

        adapter = BookingAdapter { booking ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Cancel booking for ${booking.propertyTitle}?")
                .setPositiveButton("Cancel Booking") { _, _ ->
                    viewModel.cancelBooking(booking.id, NetworkUtils.isOnline(requireContext()))
                }
                .setNegativeButton("Keep") { d, _ -> d.dismiss() }
                .show()
        }

        binding.bookingsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.bookingsRecycler.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> binding.bookingsProgress.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.bookingsProgress.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.emptyText.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is ResultState.Error -> {
                    binding.bookingsProgress.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.cancelState.observe(viewLifecycleOwner) { state ->
            if (state is ResultState.Success) {
                Snackbar.make(binding.root, "Booking cancelled", Snackbar.LENGTH_SHORT).show()
            } else if (state is ResultState.Error) {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.load(NetworkUtils.isOnline(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
