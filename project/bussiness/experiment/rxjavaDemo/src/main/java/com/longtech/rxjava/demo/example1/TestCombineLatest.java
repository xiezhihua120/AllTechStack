package com.longtech.rxjava.demo.example1;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;

public class TestCombineLatest {

    private static final String TAG = "CombineLatest";

    public static void test() {
        Observable.combineLatest(
                Observable.intervalRange(1, 4, 1, 1, TimeUnit.SECONDS)
                        .map(new Function < Long, String > () {@Override
                        public String apply(Long aLong) throws Exception {
                            String s1 = "A" + aLong;
                            Log.d(TAG, "===================A 发送的事件 " + s1);
                            return s1;
                        }
                        }),
                Observable.intervalRange(1, 5, 2, 2, TimeUnit.SECONDS)
                        .map(new Function < Long, String > () {@Override
                        public String apply(Long aLong) throws Exception {
                            String s2 = "B" + aLong;
                            Log.d(TAG, "===================B 发送的事件 " + s2);
                            return s2;
                        }
                        }),
                new BiFunction < String, String, String > () {@Override
                public String apply(String s, String s2) throws Exception {
                    String res = s + s2;
                    return res;
                }
                })
                .subscribe(new Observer < String > () {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "===================onSubscribe ");
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(TAG, "===================最终接收到的事件 " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "===================onError ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "===================onComplete ");
                    }
                });

    }
}
