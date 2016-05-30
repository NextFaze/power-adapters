package com.nextfaze.poweradapters;

import android.support.annotation.UiThread;
import lombok.NonNull;

import java.util.ArrayList;

public abstract class AbstractCondition implements Condition {

    @NonNull
    private final ArrayList<Observer> mObservers = new ArrayList<>();

    /** Returns the number of registered observers. */
    protected final int getObserverCount() {
        return mObservers.size();
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
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
    }

    @Override
    public final void registerObserver(@NonNull Observer observer) {
        if (mObservers.contains(observer)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(observer);
        if (mObservers.size() == 1) {
            onFirstObserverRegistered();
        }
    }

    @Override
    public final void unregisterObserver(@NonNull Observer observer) {
        int index = mObservers.indexOf(observer);
        if (index == -1) {
            throw new IllegalStateException("Observer was not registered.");
        }
        mObservers.remove(index);
        if (mObservers.size() == 0) {
            onLastObserverUnregistered();
        }
    }
}
