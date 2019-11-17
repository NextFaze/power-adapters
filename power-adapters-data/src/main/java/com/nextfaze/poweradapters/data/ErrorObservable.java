package com.nextfaze.poweradapters.data;

import android.util.Log;

import java.util.ArrayList;

import androidx.annotation.NonNull;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class ErrorObservable {

    private static final String TAG = "ErrorObservable";

    @NonNull
    private final ArrayList<ErrorObserver> mObservers = new ArrayList<>();

    void registerObserver(@NonNull ErrorObserver observer) {
        checkNotNull(observer, "observer");
        if (mObservers.contains(observer)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(observer);
    }

    void unregisterObserver(@NonNull ErrorObserver observer) {
        checkNotNull(observer, "observer");
        int index = mObservers.indexOf(observer);
        if (index == -1) {
            throw new IllegalStateException("Observer was not registered.");
        }
        mObservers.remove(index);
    }

    int getObserverCount() {
        return mObservers.size();
    }

    void notifyError(@NonNull Throwable e) {
        if (!mObservers.isEmpty()) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onError(e);
            }
        } else {
            Log.e(TAG, "Data error", e);
        }
    }
}
