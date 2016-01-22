package com.nextfaze.powerdata.rx;

import android.os.Handler;
import lombok.NonNull;
import rx.Subscription;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

/** Copied from Jake Wharton's RxBinding. */
abstract class MainThreadSubscription implements Subscription, Runnable {

    @NonNull
    private static final Handler sMainThread = new Handler(getMainLooper());

    @SuppressWarnings("unused") // Updated by 'unsubscribedUpdater' object.
    private volatile int mUnsubscribed;

    private static final AtomicIntegerFieldUpdater<MainThreadSubscription> mUnsubscribedUpdater =
            AtomicIntegerFieldUpdater.newUpdater(MainThreadSubscription.class, "mUnsubscribed");

    @Override
    public final boolean isUnsubscribed() {
        return mUnsubscribed != 0;
    }

    @Override
    public final void unsubscribe() {
        if (mUnsubscribedUpdater.compareAndSet(this, 0, 1)) {
            if (getMainLooper() == myLooper()) {
                onUnsubscribe();
            } else {
                sMainThread.post(this);
            }
        }
    }

    @Override
    public final void run() {
        onUnsubscribe();
    }

    protected abstract void onUnsubscribe();
}
