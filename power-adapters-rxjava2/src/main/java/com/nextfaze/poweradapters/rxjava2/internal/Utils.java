package com.nextfaze.poweradapters.rxjava2.internal;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import io.reactivex.Observable;

import java.util.concurrent.Callable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public final class Utils {

    private Utils() {
        throw new AssertionError();
    }

    @NonNull
    public static <T> Observable<T> mainThreadObservable(@NonNull Callable<T> callable) {
        return Observable.fromCallable(callable).subscribeOn(mainThread());
    }
}
