package com.studenthousing.app.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.model.CreatePropertyRequest
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class AddPropertyViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<Unit>>()
    val state: LiveData<ResultState<Unit>> = _state

    fun createProperty(request: CreatePropertyRequest) {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            _state.value = repository.createProperty(request)
        }
    }
}
