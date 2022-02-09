package com.longtech.rxjava.demo.impl.observable

import android.util.Log
import com.longtech.rxjava.demo.impl.core.Scheduler
import com.longtech.rxjava.demo.impl.core.dispose.Disposable
import com.longtech.rxjava.demo.impl.core.dispose.DisposableHelper
import com.longtech.rxjava.demo.impl.observer.Observer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * 释放操作有两个来源
 * 1、客户释放
 * 2、观察者接受完成
 */
class ObservableObserveOn<T>(
    var source: ObservableSource<T>,
    var scheduler: Scheduler,
)  {

    /**
     * 生产者、消费者两个位置，针对每一次连接都会新创建一个Worker
     * 1、在subscribe内部
     * 2、每次调用scheduler.createWorker
     */
    fun subscribe(observer: Observer<T>) {
        var worker = scheduler.createWorker()
        var parent = ObserveOnObserver<T>(observer, worker)
        source.subscribe(parent)
    }

    class ObserveOnObserver<T>(
        var downstream: Observer<T>,
        var worker: Scheduler.Worker,
    ): Observer<T>, Disposable, Runnable, AtomicInteger()  {

        @Volatile
        var done = false

        @Volatile
        var disposed = false

        @Volatile
        var error: Throwable? = null

        var upstream: Disposable? = null

        var queue: Queue<T> = ConcurrentLinkedQueue()

        override fun onSubscribe(d: Disposable) {
            if (DisposableHelper.validate(upstream, d)) {
                upstream = d
                downstream.onSubscribe(this)
            }
        }

        override fun onNext(t: T) {
            if (done || disposed) {
                return
            }
            queue.offer(t)
            Log.d("Example9", "onNext, $t")
            schedule()
        }

        override fun onComplete() {
            if (done || disposed) {
                return
            }
            done = true
            schedule()
        }

        override fun onError() {
            if (done || disposed) {
                return
            }
            done = true
            error = Throwable("ObservableObserveOn.onError")
            schedule()
        }


        fun schedule() {
            if (getAndIncrement() == 0) {
                worker.schedule(this)
            }
        }

        override fun run() {
            var missed = 1

            while (true) {
                if (checkTerminated(done, queue.isEmpty(), downstream)) {
                    return
                }
                while (true) {
                    var event: T?
                    try {
                    // 1、取出一个事件
                        event = queue.poll()
                    } catch (ex: Throwable) {
                        disposed = true
                        queue.clear()
                        worker.dispose()
                        upstream!!.dispose()
                        downstream.onError() // Todo
                        return
                    }

                    // 2、检查是否已结束（错误 || 完成）
                    if (checkTerminated(done, event == null, downstream)) {
                        return
                    }

                    // 3、通知观察者
                    if (event != null) {
                        downstream.onNext(event)
                    } else {
                        break
                    }
                }

                missed = addAndGet(-missed)
                if (missed <= 0) {
                    break
                }
            }
        }

        fun checkTerminated(done: Boolean, emptyQueue: Boolean, observer: Observer<T>): Boolean {
            if (disposed) {
                queue.clear()
                return true
            }
            if (done) {
                val e = error
                if (e != null) {
                    disposed = true
                    queue.clear()
                    observer.onError() // Todo:
                    worker.dispose()
                    return true
                } else if (emptyQueue) {
                    disposed = true
                    observer.onComplete()
                    worker.dispose()
                    return true
                }
            }
            return false
        }


        override fun dispose() {
            if (!disposed) {
                disposed = true
                upstream?.dispose()
                worker.dispose()
                queue.clear()
            }
        }

        override fun isDisposed(): Boolean {
            return disposed
        }

        override fun toByte(): Byte {
            return 0.toByte()
        }

        override fun toChar(): Char {
            return 0.toChar()
        }

        override fun toShort(): Short {
            return 0.toShort()
        }
    }
}