package com.studenthousing.app.ui.roommates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.model.RoommateDto
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class RoommatesViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<List<RoommateDto>>>()
    val state: LiveData<ResultState<List<RoommateDto>>> = _state

    private val _connectState = MutableLiveData<ResultState<Unit>>()
    val connectState: LiveData<ResultState<Unit>> = _connectState

    fun load() {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            _state.value = repository.getPotentialRoommates()
        }
    }

    fun connect(roommateId: String) {
        viewModelScope.launch {
            _connectState.value = ResultState.Loading
            _connectState.value = repository.connectRoommate(roommateId)
            load() // Refresh list to remove connected roommate
        }
    }
}
