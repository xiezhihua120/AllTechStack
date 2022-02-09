package com.longtech.rxjava.demo.impl.observable;

import com.longtech.rxjava.demo.impl.core.Scheduler;

public abstract class Observable<T> implements ObservableSource<T> {

    public static <T> Observable<T> create(ObservableOnSubscribe<T> onSubscribe) {
        return new ObservableCreate(onSubscribe);
    }

    public ObservableSubscribeOn<T> subscribeOn(Scheduler scheduler) {
        return new ObservableSubscribeOn(this, scheduler);
    }

    public ObservableObserveOn<T> observeOn(Scheduler scheduler) {
        return new ObservableObserveOn(this, scheduler);
    }
}
