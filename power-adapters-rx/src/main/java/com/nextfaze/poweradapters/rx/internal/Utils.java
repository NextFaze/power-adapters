package com.nextfaze.poweradapters.rx.internal;

import android.support.annotation.RestrictTo;
import lombok.NonNull;
import rx.Observable;

import java.util.concurrent.Callable;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static rx.Observable.fromCallable;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public final class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static void assertUiThread() {
        if (myLooper() != getMainLooper()) {
            throw new IllegalStateException("Must be called from UI thread. Current thread: " + Thread.currentThread());
        }
    }

    @NonNull
    public static <T> Observable<T> mainThreadObservable(@NonNull Callable<T> callable) {
        return fromCallable(callable).subscribeOn(mainThread());
    }
}
