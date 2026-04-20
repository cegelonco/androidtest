package com.studenthousing.app.ui.owner

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
import com.studenthousing.app.databinding.FragmentBookingRequestsBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class BookingRequestsFragment : Fragment(R.layout.fragment_booking_requests) {
    private var _binding: FragmentBookingRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookingRequestsViewModel
    private lateinit var adapter: BookingRequestAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookingRequestsBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[BookingRequestsViewModel::class.java]

        adapter = BookingRequestAdapter(
            onConfirm = { booking ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.confirm_booking))
                    .setMessage(getString(R.string.confirm_booking_msg, booking.student?.firstName ?: "student"))
                    .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                        viewModel.confirmBooking(booking._id)
                    }
                    .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                    .show()
            },
            onReject = { booking ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.reject_booking))
                    .setMessage(getString(R.string.reject_booking_msg, booking.student?.firstName ?: "student"))
                    .setPositiveButton(getString(R.string.reject)) { _, _ ->
                        viewModel.cancelBooking(booking._id)
                    }
                    .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                    .show()
            }
        )

        binding.requestsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.requestsRecycler.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> binding.progress.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.progress.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.emptyText.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is ResultState.Error -> {
                    binding.progress.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            if (state is ResultState.Success) {
                Snackbar.make(binding.root, getString(R.string.action_completed), Snackbar.LENGTH_SHORT).show()
            } else if (state is ResultState.Error) {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
