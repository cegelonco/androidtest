package com.studenthousing.app.ui.properties

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class PropertyDetailViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<PropertyEntity>>()
    val state: LiveData<ResultState<PropertyEntity>> = _state

    fun load(propertyId: String) {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            val item = repository.getPropertyById(propertyId)
            if (item != null) _state.value = ResultState.Success(item)
            else _state.value = ResultState.Error("Property not found")
        }
    }
}
