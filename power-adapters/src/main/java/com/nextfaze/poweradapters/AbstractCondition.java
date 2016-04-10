package com.nextfaze.poweradapters;

import android.support.annotation.UiThread;
import lombok.NonNull;

public abstract class AbstractCondition implements Condition {

    @NonNull
    private final Observable mObservable = new Observable();

    /** Returns the number of registered observers. */
    protected final int getObserverCount() {
        return mObservable.size();
    }

    /** Called when the first observer has registered with this condition. */
    @UiThread
    protected void onFirstObserverRegistered() {
    }

    /** Called when the last observer has unregistered from this condition. */
    @UiThread
    protected void onLastObserverUnregistered() {
    }

    /** Notify observers that the condition has changed. */
    protected final void notifyChanged() {
        mObservable.notifyChanged();
    }

    @Override
    public void registerObserver(@NonNull Observer observer) {
        boolean firstAdded;
        synchronized (mObservable) {
            mObservable.registerObserver(observer);
            firstAdded = mObservable.size() == 1;
        }
        if (firstAdded) {
            onFirstObserverRegistered();
        }
    }

    @Override
    public void unregisterObserver(@NonNull Observer observer) {
        boolean lastRemoved;
        synchronized (mObservable) {
            mObservable.unregisterObserver(observer);
            lastRemoved = mObservable.size() == 0;
        }
        if (lastRemoved) {
            onLastObserverUnregistered();
        }
    }
}
