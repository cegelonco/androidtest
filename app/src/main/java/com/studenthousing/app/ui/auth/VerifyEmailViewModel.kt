package com.studenthousing.app.ui.auth

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.data.repo.StudentHousingRepository
import kotlinx.coroutines.launch

class VerifyEmailViewModel(
    private val repository: StudentHousingRepository
) : ViewModel() {
    private val _verifyState = MutableLiveData<ResultState<Unit>>()
    val verifyState: LiveData<ResultState<Unit>> = _verifyState

    private val _resendState = MutableLiveData<ResultState<String?>>()
    val resendState: LiveData<ResultState<String?>> = _resendState

    private val _resendCooldown = MutableLiveData(0)
    val resendCooldown: LiveData<Int> = _resendCooldown

    private var cooldownTimer: CountDownTimer? = null

    fun verify(email: String, code: String) {
        _verifyState.value = ResultState.Loading
        viewModelScope.launch {
            _verifyState.value = repository.verifyEmail(email, code)
        }
    }

    fun resend(email: String) {
        if ((_resendCooldown.value ?: 0) > 0) return
        _resendState.value = ResultState.Loading
        viewModelScope.launch {
            val result = repository.resendVerification(email)
            _resendState.value = result
            if (result is ResultState.Success) {
                startCooldown()
            }
        }
    }

    private fun startCooldown() {
        cooldownTimer?.cancel()
        cooldownTimer = object : CountDownTimer(60_000L, 1_000L) {
            override fun onTick(remaining: Long) {
                _resendCooldown.value = (remaining / 1000).toInt()
            }
            override fun onFinish() {
                _resendCooldown.value = 0
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        cooldownTimer?.cancel()
    }
}
