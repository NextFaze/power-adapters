package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.addAll;

abstract class CompoundCondition extends Condition {

    @NonNull
    private final Observer mObserver = new Observer() {
        @Override
        public void onChanged() {
            notifyChanged();
        }
    };

    @NonNull
    private final Set<Condition> mDependencies;

    private boolean mObserving;

    CompoundCondition(@NonNull Set<Condition> dependencies) {
        mDependencies = new HashSet<>(dependencies);
    }

    CompoundCondition(@NonNull Condition... dependencies) {
        mDependencies = new HashSet<>(dependencies.length);
        addAll(mDependencies, dependencies);
    }

    CompoundCondition(@NonNull Condition c0) {
        mDependencies = new HashSet<>(1);
        mDependencies.add(c0);
    }

    CompoundCondition(@NonNull Condition c0, @NonNull Condition c1) {
        mDependencies = new HashSet<>(2);
        mDependencies.add(c0);
        mDependencies.add(c1);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        updateObserver();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        updateObserver();
    }

    private void updateObserver() {
        boolean observe = getObserverCount() > 0;
        if (observe != mObserving) {
            mObserving = observe;
            if (mObserving) {
                for (Condition condition : mDependencies) {
                    condition.registerObserver(mObserver);
                }
            } else {
                for (Condition condition : mDependencies) {
                    condition.unregisterObserver(mObserver);
                }
            }
        }
    }
}
