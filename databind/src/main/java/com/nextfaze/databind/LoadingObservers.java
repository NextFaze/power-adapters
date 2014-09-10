package com.nextfaze.databind;

import lombok.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class LoadingObservers {

    @NonNull
    private final Set<LoadingObserver> mObservers = new CopyOnWriteArraySet<LoadingObserver>();

    void register(@NonNull LoadingObserver loadingObserver) {
        mObservers.add(loadingObserver);
    }

    void unregister(@NonNull LoadingObserver loadingObserver) {
        mObservers.remove(loadingObserver);
    }

    void notifyLoadingChanged() {
        for (LoadingObserver loadingObserver : mObservers) {
            loadingObserver.onLoadingChange();
        }
    }
}
