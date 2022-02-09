package com.longtech.rxjava4retrofit.demo.example4

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.longtech.rxjava4retrofit.applike.demo.ViewProvider
import com.longtech.rxjava4retrofit.demo.RetrofitManager
import com.longtech.rxjava4retrofit.demo.example1.UserObservableService
import com.longtech.rxjava4retrofit.demo.example4.mvp.Book
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody
import com.longtech.rxjava4retrofit.demo.example4.mvp.BookPresenter
import com.longtech.rxjava4retrofit.demo.example4.mvp.BookView


class Example4 : ViewProvider.ViewHolder, BookView {
    val TAG: String = Example4::class.java.simpleName

    override fun title(): String {
        return TAG
    }

    private var context: Context? = null
    private var mBookPresenter: BookPresenter? = null

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        this.context = context
        mBookPresenter = BookPresenter(context)
        mBookPresenter?.attachView(this)
        mBookPresenter?.onCreate()
    }

    override fun onStart() {
        mBookPresenter?.onStart()
        mBookPresenter?.getSearchBooks(null, null, 0, 0)
    }

    override fun onResume() {

    }

    override fun onPause() {
        mBookPresenter?.pause()
    }

    override fun onDestory() {
        mBookPresenter?.onStop()
    }

    override fun onClick(view: View) {

    }

    //---------------------------------------------------------------------------------------------//
    override fun onSuccess(mBook: Book?) {
        Toast.makeText(context, mBook?.name, Toast.LENGTH_SHORT).show()
    }

    override fun onError(result: String?) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
    }
}