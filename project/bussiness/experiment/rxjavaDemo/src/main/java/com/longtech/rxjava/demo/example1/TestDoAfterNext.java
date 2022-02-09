package com.longtech.rxjava.demo.example1;

import android.util.Log;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Consumer;

public class TestDoAfterNext {

    private static final String TAG = "TestDoAfterNext";

    /**
     * scan迭代的每一次都会进入Consumer.accept
     * reduce迭代的最后出现结果，才进入Consumer.accept
     */
    public static void test() {
        Observable.create(new ObservableOnSubscribe< Integer >() {
            @Override
            public void subscribe(ObservableEmitter< Integer > e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        })
                .doAfterNext(new Consumer < Integer > () {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "==================doAfterNext " + integer);
                    }
                })
                .subscribe(new Observer< Integer >() {
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
