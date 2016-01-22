package com.nextfaze.powerdata.rx;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

final class ThreadUtils {

    private ThreadUtils() {
    }

    static void assertUiThread() {
        if (myLooper() != getMainLooper()) {
            throw new IllegalStateException("Must be called from UI thread. Current thread: " + Thread.currentThread());
        }
    }
}
