package com.studenthousing.app.ui.roommates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentRoommatesBinding
import com.studenthousing.app.ui.CommonViewModelFactory

class RoommatesFragment : Fragment(R.layout.fragment_roommates) {
    private var _binding: FragmentRoommatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RoommatesViewModel
    private lateinit var adapter: RoommateAdapter
    private val skippedIds = mutableSetOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRoommatesBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[RoommatesViewModel::class.java]

        adapter = RoommateAdapter(
            onConnect = { roommate ->
                val userId = roommate.student?._id ?: roommate._id
                viewModel.connect(userId)
            },
            onSkip = { roommate ->
                skippedIds.add(roommate._id)
                val current = (viewModel.state.value as? ResultState.Success)?.data ?: return@RoommateAdapter
                adapter.submitList(current.filter { it._id !in skippedIds })
            }
        )

        binding.roommatesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.roommatesRecycler.adapter = adapter

        binding.btnConnections.setOnClickListener {
            findNavController().navigate(R.id.connectionsFragment)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> binding.progress.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.progress.visibility = View.GONE
                    val filtered = state.data.filter { it._id !in skippedIds }
                    adapter.submitList(filtered)
                    binding.emptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                }
                is ResultState.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.emptyText.visibility = View.VISIBLE
                    binding.emptyText.text = state.message
                }
            }
        }

        viewModel.connectState.observe(viewLifecycleOwner) { state ->
            if (state is ResultState.Success) {
                Snackbar.make(binding.root, getString(R.string.connection_sent), Snackbar.LENGTH_SHORT).show()
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
