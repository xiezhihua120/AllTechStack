package com.longtech.rxjava4retrofit.demo.example1

import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET

interface UserObservableService {

    @GET("/")
    fun requestUser(): Observable<ResponseBody>

}