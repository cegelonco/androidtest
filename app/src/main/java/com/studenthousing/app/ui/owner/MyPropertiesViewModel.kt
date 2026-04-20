package com.studenthousing.app.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.local.PropertyEntity
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class MyPropertiesViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<List<PropertyEntity>>>()
    val state: LiveData<ResultState<List<PropertyEntity>>> = _state

    private val _deleteState = MutableLiveData<ResultState<Unit>>()
    val deleteState: LiveData<ResultState<Unit>> = _deleteState

    fun load() {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            _state.value = repository.getMyProperties()
        }
    }

    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            _deleteState.value = repository.deleteProperty(propertyId)
            load() // Refresh list
        }
    }
}
