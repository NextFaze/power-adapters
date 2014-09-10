package com.nextfaze.databind;

import lombok.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class ErrorObservers {
    
    @NonNull
    private final Set<ErrorObserver> mObservers = new CopyOnWriteArraySet<ErrorObserver>();

    void register(@NonNull ErrorObserver errorObserver) {
        mObservers.add(errorObserver);
    }

    void unregister(@NonNull ErrorObserver errorObserver) {
        mObservers.remove(errorObserver);
    }

    void notifyError(@NonNull Throwable e) {
        for (ErrorObserver errorObserver : mObservers) {
            errorObserver.onError(e);
        }
    }
}
