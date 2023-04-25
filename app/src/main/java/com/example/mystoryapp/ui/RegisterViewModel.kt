package com.example.mystoryapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mystoryapp.retrofit.ApiConfig
import com.example.mystoryapp.retrofit.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    var isError: Boolean = false

    fun getRegisterUser(name: String, email: String, password: String) {
        _isLoading.value = true
        val api = ApiConfig.getApiService().registerUser(name, email, password)
        api.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                _isLoading.value = false
                val responseBody = response.body()
                if (response.isSuccessful) {
                    isError = false
                    _message.value = responseBody?.message.toString()
                } else {
                    isError = true
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                isError = true
                _isLoading.value = false
            }
        })
    }
}