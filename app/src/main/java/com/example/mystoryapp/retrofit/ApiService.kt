package com.example.mystoryapp.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ) : Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ) : Call<LoginResponse>

    @GET("stories?location=1")
    fun getStories(@Header("Authorization") token: String) : Call<StoryResponse>

    @Multipart
    @POST("stories")
    fun addStoryUser(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Float?,
        @Part("lon") lon: Float?,
        @Header("Authorization") token: String
    ) : Call<AddStoryResponse>

    @GET("stories")
    suspend fun getPagingStories(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int,
        @Header("Authorization") token: String
    ) : StoryResponseItem
}