package com.studenthousing.app.ui.roommates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentConnectionsBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class ConnectionsFragment : Fragment(R.layout.fragment_connections) {
    private var _binding: FragmentConnectionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ConnectionsViewModel
    private lateinit var connectionAdapter: ConnectionAdapter
    private lateinit var requestAdapter: RequestAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConnectionsBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[ConnectionsViewModel::class.java]

        connectionAdapter = ConnectionAdapter()
        binding.connectionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.connectionsRecycler.adapter = connectionAdapter

        requestAdapter = RequestAdapter(
            onAccept = { request ->
                request.connectionId?.let { viewModel.acceptRequest(it) }
            },
            onReject = { request ->
                request.connectionId?.let { viewModel.rejectRequest(it) }
            }
        )
        binding.requestsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.requestsRecycler.adapter = requestAdapter

        // Observe incoming requests
        viewModel.requests.observe(viewLifecycleOwner) { state ->
            if (state is ResultState.Success) {
                val requests = state.data
                if (requests.isNotEmpty()) {
                    binding.requestsHeader.visibility = View.VISIBLE
                    binding.requestsRecycler.visibility = View.VISIBLE
                    requestAdapter.submitList(requests)
                } else {
                    binding.requestsHeader.visibility = View.GONE
                    binding.requestsRecycler.visibility = View.GONE
                }
            }
        }

        // Observe connections
        viewModel.connections.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> binding.progress.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.progress.visibility = View.GONE
                    connectionAdapter.submitList(state.data)
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
