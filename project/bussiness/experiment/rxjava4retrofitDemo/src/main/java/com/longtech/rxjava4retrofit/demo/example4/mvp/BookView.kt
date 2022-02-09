package com.longtech.rxjava4retrofit.demo.example4.mvp

import com.longtech.rxjava4retrofit.demo.example4.mvp.base.IView

interface BookView: IView {
    fun onSuccess(mBook: Book?)
    fun onError(result: String?)
}