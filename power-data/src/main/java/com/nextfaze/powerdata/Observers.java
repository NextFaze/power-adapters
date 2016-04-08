package com.nextfaze.powerdata;

import lombok.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

abstract class Observers<T> {

    @NonNull
    final Set<T> mObservers = new CopyOnWriteArraySet<>();

    final void register(@NonNull T t) {
        mObservers.add(t);
    }

    final void unregister(@NonNull T t) {
        mObservers.remove(t);
    }

    final int size() {
        return mObservers.size();
    }
}
