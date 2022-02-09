package com.longtech.rxjava.demo.impl.core.thread;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public final class SchedulerPoolFactory {
    /** Utility class. */
    private SchedulerPoolFactory() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Indicates the periodic purging of the ScheduledExecutorService is enabled.
     */
    public static final boolean PURGE_ENABLED;

    /**
     * Indicates the purge period of the ScheduledExecutorServices created by create().
     */
    public static final int PURGE_PERIOD_SECONDS;

    static final AtomicReference<ScheduledExecutorService> PURGE_THREAD = new AtomicReference<>();

    // Upcast to the Map interface here to avoid 8.x compatibility issues.
    // See http://stackoverflow.com/a/32955708/61158
    static final Map<ScheduledThreadPoolExecutor, Object> POOLS = new ConcurrentHashMap<>();

    /**
     * Starts the purge thread if not already started.
     */
    public static void start() {
        tryStart(PURGE_ENABLED);
    }

    /**
     * 初始化就开启了一个定时器，用于定时清理被shutdown掉的线程池
     * @param purgeEnabled
     */
    static void tryStart(boolean purgeEnabled) {
        if (purgeEnabled) {
            for (;;) {
                ScheduledExecutorService curr = PURGE_THREAD.get();
                if (curr != null) {
                    return;
                }
                ScheduledExecutorService next = Executors.newScheduledThreadPool(1, new RxThreadFactory("RxSchedulerPurge"));
                if (PURGE_THREAD.compareAndSet(curr, next)) {

                    next.scheduleAtFixedRate(new ScheduledTask(), PURGE_PERIOD_SECONDS, PURGE_PERIOD_SECONDS, TimeUnit.SECONDS);

                    return;
                } else {
                    next.shutdownNow();
                }
            }
        }
    }

    /**
     * Stops the purge thread.
     */
    public static void shutdown() {
        ScheduledExecutorService exec = PURGE_THREAD.getAndSet(null);
        if (exec != null) {
            exec.shutdownNow();
        }
        POOLS.clear();
    }

    static {
        PURGE_ENABLED = true;
        PURGE_PERIOD_SECONDS = 1;

        start();
    }

    /**
     * Creates a ScheduledExecutorService with the given factory.
     * @param factory the thread factory
     * @return the ScheduledExecutorService
     */
    public static ScheduledExecutorService create(ThreadFactory factory) {
        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1, factory);
        tryPutIntoPool(PURGE_ENABLED, exec);
        return exec;
    }

    static void tryPutIntoPool(boolean purgeEnabled, ScheduledExecutorService exec) {
        if (purgeEnabled && exec instanceof ScheduledThreadPoolExecutor) {
            ScheduledThreadPoolExecutor e = (ScheduledThreadPoolExecutor) exec;
            POOLS.put(e, exec);
        }
    }

    static final class ScheduledTask implements Runnable {
        @Override
        public void run() {
            for (ScheduledThreadPoolExecutor e : new ArrayList<>(POOLS.keySet())) {
                if (e.isShutdown()) {
                    POOLS.remove(e);
                } else {
                    e.purge();
                }
            }
        }
    }
}
