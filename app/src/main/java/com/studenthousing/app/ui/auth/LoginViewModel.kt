package com.studenthousing.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _loginState = MutableLiveData<ResultState<Unit>>()
    val loginState: LiveData<ResultState<Unit>> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = ResultState.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(email, password)
        }
    }
}
