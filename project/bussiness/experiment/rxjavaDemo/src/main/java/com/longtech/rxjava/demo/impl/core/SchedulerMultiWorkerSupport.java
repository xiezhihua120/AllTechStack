package com.longtech.rxjava.demo.impl.core;


import androidx.annotation.NonNull;

public interface SchedulerMultiWorkerSupport {

    /**
     * Creates the given number of {@link io.reactivex.rxjava3.core.Scheduler.Worker} instances
     * that are possibly backed by distinct threads
     * and calls the specified {@code Consumer} with them.
     * @param number the number of workers to create, positive
     * @param callback the callback to send worker instances to
     */
    void createWorkers(int number, @NonNull SchedulerMultiWorkerSupport.WorkerCallback callback);

    /**
     * The callback interface for the {@link SchedulerMultiWorkerSupport#createWorkers(int, SchedulerMultiWorkerSupport.WorkerCallback)}
     * method.
     */
    interface WorkerCallback {
        /**
         * Called with the Worker index and instance.
         * @param index the worker index, zero-based
         * @param worker the worker instance
         */
        void onWorker(int index, @NonNull Scheduler.Worker worker);
    }
}
