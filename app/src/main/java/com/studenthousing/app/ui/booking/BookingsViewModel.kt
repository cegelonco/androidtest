package com.studenthousing.app.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.local.BookingEntity
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class BookingsViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<List<BookingEntity>>>()
    val state: LiveData<ResultState<List<BookingEntity>>> = _state

    private val _cancelState = MutableLiveData<ResultState<Unit>>()
    val cancelState: LiveData<ResultState<Unit>> = _cancelState

    fun load(online: Boolean) {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            _state.value = repository.loadBookings(online)
        }
    }

    fun createBooking(propertyId: String) {
        viewModelScope.launch {
            repository.createBooking(propertyId)
        }
    }

    fun cancelBooking(bookingId: String, online: Boolean) {
        viewModelScope.launch {
            _cancelState.value = repository.cancelBooking(bookingId)
            // Reload bookings after cancel
            load(online)
        }
    }
}
