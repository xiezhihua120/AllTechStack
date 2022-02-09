package com.longtech.rxjava4retrofit.demo.example1


import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface UserCallService {

    @GET("http://www.baidu.com")
    fun requestUser(): Observable<ResponseData>

}

data class ResponseData(
    var code: Int? = -1,
    var message: String? = null,
    var success: String? = null
)