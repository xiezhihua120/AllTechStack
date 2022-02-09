package com.longtech.rxjava.demo.example1;

import android.util.Log;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.BooleanSupplier;
import io.reactivex.rxjava3.functions.Consumer;

public class TestRetryUntil {

    private static final String TAG = "TestReduce";

    /**
     * scan迭代的每一次都会进入Consumer.accept
     * reduce迭代的最后出现结果，才进入Consumer.accept
     */
    public static void test() {
        final int[] i = {0};
        Observable.create(new ObservableOnSubscribe< Integer >() {
            @Override
            public void subscribe(ObservableEmitter< Integer > e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onError(new Exception("404"));
            }
        })
                .retryUntil(new BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() throws Exception {
                        if (i[0] == 6) {
                            return true;
                        }
                        return false;
                    }
                })
                .subscribe(new Observer< Integer >() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "==================onSubscribe ");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        i[0] += integer;
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
