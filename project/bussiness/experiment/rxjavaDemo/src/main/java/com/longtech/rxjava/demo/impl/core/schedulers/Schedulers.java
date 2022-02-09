package com.longtech.rxjava.demo.impl.core.schedulers;

import com.longtech.rxjava.demo.impl.core.Scheduler;
import com.longtech.rxjava.demo.impl.core.schedulers.io.IoScheduler;
import com.longtech.rxjava.demo.impl.core.schedulers.newthread.NewThreadScheduler;
import com.longtech.rxjava.demo.impl.core.schedulers.single.SingleScheduler;

public final class Schedulers {

    public static final Scheduler SINGLE = SingleHolder.DEFAULT;

    public static final Scheduler COMPUTATION = ComputationHolder.DEFAULT;

    public static final Scheduler IO = IoHolder.DEFAULT;

    public static final Scheduler NEW_THREAD = NewThreadHolder.DEFAULT;

    private Schedulers() {
        throw new IllegalStateException("No instances!");
    }

    static final class SingleHolder {
        static final Scheduler DEFAULT = new SingleScheduler();
    }

    static final class ComputationHolder {
        static final Scheduler DEFAULT = null;          //new ComputationScheduler();
    }

    static final class IoHolder {
        static final Scheduler DEFAULT =new IoScheduler();
    }

    static final class NewThreadHolder {
        static final Scheduler DEFAULT = new NewThreadScheduler();
    }
}
