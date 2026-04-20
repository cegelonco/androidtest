package com.studenthousing.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.model.RegisterRequest
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class SignupViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _registerState = MutableLiveData<ResultState<Pair<String, String?>>>()
    val registerState: LiveData<ResultState<Pair<String, String?>>> = _registerState

    fun register(request: RegisterRequest) {
        _registerState.value = ResultState.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(request)
        }
    }
}
