package com.nextfaze.poweradapters.data;

import lombok.NonNull;

import java.util.ArrayList;

final class LoadingObservable {

    @NonNull
    private final ArrayList<LoadingObserver> mObservers = new ArrayList<>();

    void registerObserver(@NonNull LoadingObserver observer) {
        if (mObservers.contains(observer)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(observer);
    }

    void unregisterObserver(@NonNull LoadingObserver observer) {
        int index = mObservers.indexOf(observer);
        if (index == -1) {
            throw new IllegalStateException("Observer was not registered.");
        }
        mObservers.remove(index);
    }

    int getObserverCount() {
        return mObservers.size();
    }

    void notifyLoadingChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onLoadingChange();
        }
    }
}
