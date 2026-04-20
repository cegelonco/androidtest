package com.studenthousing.app.ui.owner

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentMyPropertiesBinding
import com.studenthousing.app.ui.CommonViewModelFactory
import com.studenthousing.app.ui.properties.PropertyAdapter

class MyPropertiesFragment : Fragment(R.layout.fragment_my_properties) {
    private var _binding: FragmentMyPropertiesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyPropertiesViewModel
    private lateinit var adapter: PropertyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyPropertiesBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[MyPropertiesViewModel::class.java]

        adapter = PropertyAdapter(
            onClick = { property ->
                showPropertyActions(property)
            }
        )
        binding.propertiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.propertiesRecycler.adapter = adapter

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

        viewModel.deleteState.observe(viewLifecycleOwner) { state ->
            if (state is ResultState.Success) {
                Snackbar.make(binding.root, getString(R.string.property_deleted), Snackbar.LENGTH_SHORT).show()
            } else if (state is ResultState.Error) {
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.load()
    }

    private fun showPropertyActions(property: PropertyEntity) {
        val actions = arrayOf(
            getString(R.string.view_details),
            getString(R.string.delete_property)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(property.title)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> findNavController().navigate(
                        R.id.propertyDetailFragment,
                        Bundle().apply { putString("property_id", property.id) }
                    )
                    1 -> confirmDelete(property)
                }
            }
            .show()
    }

    private fun confirmDelete(property: PropertyEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_property))
            .setMessage(getString(R.string.delete_property_confirm, property.title))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteProperty(property.id)
            }
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
