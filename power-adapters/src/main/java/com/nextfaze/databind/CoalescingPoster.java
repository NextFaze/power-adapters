package com.nextfaze.databind;

import android.os.Handler;
import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.os.Looper.getMainLooper;

final class CoalescingPoster {

    @NonNull
    private final Handler mHandler;

    @NonNull
    private final Queue<Runnable> mRunnableQueue = new ConcurrentLinkedQueue<Runnable>();

    @NonNull
    private final Runnable mFlushRunnable = new Runnable() {
        @Override
        public void run() {
            flush();
        }
    };

    @NonNull
    private final AtomicBoolean mPosted = new AtomicBoolean();

    CoalescingPoster() {
        this(new Handler(getMainLooper()));
    }

    CoalescingPoster(@NonNull Handler handler) {
        mHandler = handler;
    }

    void post(@NonNull Runnable runnable) {
        mRunnableQueue.offer(runnable);
        if (mPosted.compareAndSet(false, true)) {
            mHandler.post(mFlushRunnable);
        }
    }

    private void flush() {
        Runnable runnable;
        while ((runnable = mRunnableQueue.poll()) != null) {
            runnable.run();
        }
        mPosted.set(false);
    }
}
