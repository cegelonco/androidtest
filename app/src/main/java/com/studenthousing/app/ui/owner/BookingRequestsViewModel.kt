package com.studenthousing.app.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.model.BookingDto
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class BookingRequestsViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<List<BookingDto>>>()
    val state: LiveData<ResultState<List<BookingDto>>> = _state

    private val _actionState = MutableLiveData<ResultState<Unit>>()
    val actionState: LiveData<ResultState<Unit>> = _actionState

    fun load() {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            _state.value = repository.getOwnerBookingRequests()
        }
    }

    fun confirmBooking(bookingId: String) {
        viewModelScope.launch {
            _actionState.value = repository.confirmBooking(bookingId)
            load()
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _actionState.value = repository.cancelBooking(bookingId)
            load()
        }
    }
}
