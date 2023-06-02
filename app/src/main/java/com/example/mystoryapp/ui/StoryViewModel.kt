package com.example.mystoryapp.ui

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mystoryapp.data.StoryRepository
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.retrofit.ApiConfig
import com.example.mystoryapp.retrofit.ListStoryPagingResponse
import com.example.mystoryapp.retrofit.ListStoryResponse
import com.example.mystoryapp.retrofit.StoryResponse
import retrofit2.Call
import retrofit2.Response

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    var story: List<ListStoryPagingResponse> = listOf()

    var storyy: List<ListStoryResponse> = listOf()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    var isError: Boolean = false

    fun getStory(token: String): LiveData<PagingData<ListStoryPagingResponse>> {
        return storyRepository.getStory(token).cachedIn(viewModelScope)
    }

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
                        storyy = responseBody.listStory
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

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryViewModel(Injection.provideRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}