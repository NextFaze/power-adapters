package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class AvailableObservable {

    @NonNull
    private final ArrayList<AvailableObserver> mObservers = new ArrayList<>();

    void registerObserver(@NonNull AvailableObserver observer) {
        checkNotNull(observer, "observer");
        if (mObservers.contains(observer)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(observer);
    }

    void unregisterObserver(@NonNull AvailableObserver observer) {
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

    void notifyAvailableChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onAvailableChange();
        }
    }
}
