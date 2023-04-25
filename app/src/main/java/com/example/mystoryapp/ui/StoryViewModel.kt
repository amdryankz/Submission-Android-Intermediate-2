package com.example.mystoryapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mystoryapp.retrofit.ApiConfig
import com.example.mystoryapp.retrofit.ListStoryResponse
import com.example.mystoryapp.retrofit.StoryResponse
import retrofit2.Call
import retrofit2.Response

class StoryViewModel : ViewModel() {
    var story: List<ListStoryResponse> = listOf()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    var isError: Boolean = false

    fun getStories(token: String) {
        _isLoading.value = true
        val api = ApiConfig.getApiService().getStories("Bearer $token")
        api.enqueue(object : retrofit2.Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    isError = false
                    val responseBody = response.body()
                    if (responseBody != null) {
                        story = responseBody.listStory
                    }
                    _message.value = responseBody?.message.toString()

                } else {
                    isError = true
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                _isLoading.value = false
                isError = true
                _message.value = t.message.toString()
            }
        })
    }
}