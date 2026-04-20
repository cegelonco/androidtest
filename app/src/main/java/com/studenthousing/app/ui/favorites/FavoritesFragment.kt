package com.studenthousing.app.ui.favorites

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
import com.studenthousing.app.databinding.FragmentFavoritesBinding
import com.studenthousing.app.ui.CommonViewModelFactory
import com.studenthousing.app.ui.properties.PropertyAdapter

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavoritesViewModel
    private lateinit var adapter: PropertyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoritesBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[FavoritesViewModel::class.java]

        adapter = PropertyAdapter { property ->
            findNavController().navigate(
                R.id.propertyDetailFragment,
                Bundle().apply { putString("property_id", property.id) }
            )
        }
        binding.favoritesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesRecycler.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> binding.favoritesProgress.visibility = View.VISIBLE
                is ResultState.Success -> {
                    binding.favoritesProgress.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.emptyText.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                    binding.favoritesRecycler.visibility = if (state.data.isEmpty()) View.GONE else View.VISIBLE
                }
                is ResultState.Error -> {
                    binding.favoritesProgress.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
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
