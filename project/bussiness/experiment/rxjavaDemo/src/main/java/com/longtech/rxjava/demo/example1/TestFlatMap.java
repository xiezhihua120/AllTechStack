package com.longtech.rxjava.demo.example1;

import android.util.Log;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Function3;

public class TestFlatMap {

    private static final String TAG = "TestFlatMap";

    public static void test() {
        Observable.just(1,2,3,4,5,6)
                .flatMap(new Function<Integer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(Integer number) {
                        return Observable.just("[" + number);
                    }
                })
                .flatMap(new Function <String, ObservableSource <String>> () {
                    @Override
                    public ObservableSource<String> apply(String plan) throws Exception {
                        return Observable.just(plan + "]");
                    }
                })
                .subscribe(new Observer<String> () {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(TAG, "==================action: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}
