package com.nextfaze.poweradapters.rx.internal;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

public final class ThreadUtils {

    private ThreadUtils() {
        throw new AssertionError();
    }

    public static void assertUiThread() {
        if (myLooper() != getMainLooper()) {
            throw new IllegalStateException("Must be called from UI thread. Current thread: " + Thread.currentThread());
        }
    }
}
