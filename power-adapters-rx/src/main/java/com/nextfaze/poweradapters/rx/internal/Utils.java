package com.nextfaze.poweradapters.rx.internal;

import lombok.NonNull;
import rx.Observable;

import java.util.concurrent.Callable;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;
import static rx.Observable.fromCallable;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

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
