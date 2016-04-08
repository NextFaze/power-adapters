package com.nextfaze.powerdata;

import android.os.Handler;
import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.os.Looper.getMainLooper;

final class CoalescingPoster {

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @NonNull
    private final Queue<Runnable> mRunnableQueue = new ConcurrentLinkedQueue<>();

    @NonNull
    private final Runnable mFlushRunnable = new Runnable() {
        @Override
        public void run() {
            flush();
        }
    };

    @NonNull
    private final AtomicBoolean mPosted = new AtomicBoolean();

    void post(@NonNull Runnable runnable) {
        mRunnableQueue.offer(runnable);
        if (mPosted.compareAndSet(false, true)) {
            mHandler.post(mFlushRunnable);
        }
    }

    void dispose() {
        mHandler.removeCallbacks(mFlushRunnable);
    }

    private void flush() {
        Runnable runnable;
        while ((runnable = mRunnableQueue.poll()) != null) {
            runnable.run();
        }
        mPosted.set(false);
    }
}
