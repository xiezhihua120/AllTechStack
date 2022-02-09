package com.longtech.taskmanager.practice.core;

import com.longtech.taskmanager.practice.core.dispose.Disposable;

import java.util.concurrent.TimeUnit;


public abstract class Scheduler {

    public void start() {

    }

    public void shutdown() {

    }

    //--------------------------------------------------------------------------------------------//

    public Disposable scheduleDirect(Runnable run) {
        return scheduleDirect(run, 0L, TimeUnit.NANOSECONDS);
    }

    /**
     * Scheduler对外的核心方法：输入runnable，输出一个可控的disposable
     * 其中：Scheduler中的Disposable，实际上是利用DiposeTask来释放Worker的，当然Worker也可以直接释放
     * @param run
     * @param delay
     * @param unit
     * @return
     */
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        final Worker w = createWorker();

        DisposeTask task = new DisposeTask(run, w);

        w.schedule(task, delay, unit);

        return task;
    }

    /**
     * Scheduler对内的核心方法：构建一个Worker
     * @return
     */
    public abstract Worker createWorker();

    //--------------------------------------------------------------------------------------------//

    /**
     * Worker最重要的功能：替Scheduler实现schedule方法
     */
    public abstract static class Worker implements Disposable {

        public Disposable schedule(Runnable run) {
            return schedule(run, 0L, TimeUnit.NANOSECONDS);
        }


        public abstract Disposable schedule(Runnable run, long delay, TimeUnit unit);
    }


    /**
     * DisposeTask承载两方便功能：1、内部代理了实际的runnable   2、外表提供了控制Woker进行dispose的能力
     */
    static final class DisposeTask implements Disposable, Runnable {

        final Runnable decoratedRun;

        final Worker w;

        Thread runner;

        DisposeTask(Runnable decoratedRun, Worker w) {
            this.decoratedRun = decoratedRun;
            this.w = w;
        }

        @Override
        public void run() {
            runner = Thread.currentThread();
            try {
                try {
                    decoratedRun.run();
                } catch (Throwable ex) {
                    throw ex;
                }
            } finally {
                dispose();
                runner = null;
            }
        }

        @Override
        public void dispose() {
            w.dispose();
        }

        @Override
        public boolean isDisposed() {
            return w.isDisposed();
        }
    }

    //--------------------------------------------------------------------------------------------//
}
