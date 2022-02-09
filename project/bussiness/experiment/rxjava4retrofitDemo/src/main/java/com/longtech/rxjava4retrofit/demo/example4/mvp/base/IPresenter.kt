package com.longtech.rxjava4retrofit.demo.example4.mvp.base

import android.content.Intent

interface IPresenter {

    fun onCreate()

    fun onStart() //暂时没用到

    fun onStop()

    fun pause() //暂时没用到

    fun attachView(IView: IView)

    fun attachIncomingIntent(intent: Intent?) //暂时没用到

}