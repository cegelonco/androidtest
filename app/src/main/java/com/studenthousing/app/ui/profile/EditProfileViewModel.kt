package com.studenthousing.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.model.UpdateProfileRequest
import com.studenthousing.app.data.model.UserProfileDto
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {

    private val _updateState = MutableLiveData<ResultState<UserProfileDto>>()
    val updateState: LiveData<ResultState<UserProfileDto>> = _updateState

    fun updateProfile(phone: String, university: String, department: String) {
        _updateState.value = ResultState.Loading
        viewModelScope.launch {
            val result = repository.updateProfile(
                UpdateProfileRequest(
                    phone = phone.ifBlank { null },
                    university = university.ifBlank { null },
                    department = department.ifBlank { null }
                )
            )
            _updateState.value = result
        }
    }
}
