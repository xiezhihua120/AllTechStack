package com.longtech.taskmanager.practice.observable;

import com.longtech.taskmanager.practice.core.Scheduler;
import com.longtech.taskmanager.practice.observable.ObservableCreate;
import com.longtech.taskmanager.practice.observable.ObservableObserveOn;
import com.longtech.taskmanager.practice.observable.ObservableOnSubscribe;
import com.longtech.taskmanager.practice.observable.ObservableSource;
import com.longtech.taskmanager.practice.observable.ObservableSubscribeOn;

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
