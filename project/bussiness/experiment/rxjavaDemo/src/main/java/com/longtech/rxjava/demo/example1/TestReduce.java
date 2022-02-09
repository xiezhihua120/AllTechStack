package com.longtech.rxjava.demo.example1;

import android.util.Log;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Consumer;

public class TestReduce {

    private static final String TAG = "TestReduce";

    /**
     * scan迭代的每一次都会进入Consumer.accept
     * reduce迭代的最后出现结果，才进入Consumer.accept
     */
    public static void test() {
        Observable.just(0, 1, 2, 3)
                .reduce(new BiFunction < Integer, Integer, Integer > () {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        int res = integer + integer2;
                        Log.d(TAG, "====================integer " + integer);
                        Log.d(TAG, "====================integer2 " + integer2);
                        Log.d(TAG, "====================res " + res);
                        return res;
                    }
                })
                .subscribe(new Consumer< Integer >() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "==================accept " + integer);
                    }
                });
    }
}
