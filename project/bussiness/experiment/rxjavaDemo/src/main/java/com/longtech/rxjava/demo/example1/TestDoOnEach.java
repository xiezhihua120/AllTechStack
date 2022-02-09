package com.longtech.rxjava.demo.example1;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Notification;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

public class TestDoOnEach {

    private static final String TAG = "TestDoOnEach";

    /**
     * 从结果就可以看出每发送一个事件之前都会回调 doOnEach 方法，并且可以取出 onNext() 发送的值。
     */
    public static void test() {
        Observable.create(new ObservableOnSubscribe< Integer >() {
            @Override
            public void subscribe(ObservableEmitter< Integer > e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                //      e.onError(new NumberFormatException());
                e.onComplete();
            }
        })
                .doOnEach(new Consumer<Notification< Integer >>() {
                    @Override
                    public void accept(Notification < Integer > integerNotification) throws Exception {
                        Log.d(TAG, "==================doOnEach " + integerNotification.getValue());
                    }
                })
                .subscribe(new Observer < Integer > () {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "==================onSubscribe ");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "==================onNext " + integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "==================onError ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "==================onComplete ");
                    }
                });

    }
}
