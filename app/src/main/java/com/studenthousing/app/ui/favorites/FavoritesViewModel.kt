package com.studenthousing.app.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<List<PropertyEntity>>>()
    val state: LiveData<ResultState<List<PropertyEntity>>> = _state

    fun load() {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            _state.value = repository.getFavorites()
        }
    }

    fun toggleFavorite(propertyId: String, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(propertyId, currentlyFavorite)
            load() // Refresh the list
        }
    }
}
