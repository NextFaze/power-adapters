package com.nextfaze.databind;

import lombok.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class DataObservers {

    @NonNull
    private final Set<DataObserver> mObservers = new CopyOnWriteArraySet<DataObserver>();

    void register(@NonNull DataObserver dataObserver) {
        mObservers.add(dataObserver);
    }

    void unregister(@NonNull DataObserver dataObserver) {
        mObservers.remove(dataObserver);
    }

    void notifyDataChanged() {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onChange();
        }
    }

    void notifyDataInvalidated() {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onInvalidated();
        }
    }
}
