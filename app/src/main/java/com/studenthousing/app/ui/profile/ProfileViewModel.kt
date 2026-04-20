package com.studenthousing.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.model.UserProfileDto
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _state = MutableLiveData<ResultState<UserProfileDto>>()
    val state: LiveData<ResultState<UserProfileDto>> = _state

    fun load() {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            _state.value = repository.getProfile()
        }
    }
}
