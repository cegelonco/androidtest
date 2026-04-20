package com.studenthousing.app.ui.roommates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.model.ConnectionDto
import com.studenthousing.app.data.model.RoommateRequestDto
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class ConnectionsViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _connections = MutableLiveData<ResultState<List<ConnectionDto>>>()
    val connections: LiveData<ResultState<List<ConnectionDto>>> = _connections

    private val _requests = MutableLiveData<ResultState<List<RoommateRequestDto>>>()
    val requests: LiveData<ResultState<List<RoommateRequestDto>>> = _requests

    private val _actionState = MutableLiveData<ResultState<Unit>>()
    val actionState: LiveData<ResultState<Unit>> = _actionState

    fun load() {
        _connections.value = ResultState.Loading
        _requests.value = ResultState.Loading
        viewModelScope.launch {
            _connections.value = repository.getRoommateConnections()
            _requests.value = repository.getRoommateRequests()
        }
    }

    fun acceptRequest(connectionId: String) {
        viewModelScope.launch {
            _actionState.value = repository.respondToRoommateRequest(connectionId, true)
            load()
        }
    }

    fun rejectRequest(connectionId: String) {
        viewModelScope.launch {
            _actionState.value = repository.respondToRoommateRequest(connectionId, false)
            load()
        }
    }
}
