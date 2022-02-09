package com.longtech.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {

    @GET("/")
    fun requestUser(): Call<ResponseBody>

}