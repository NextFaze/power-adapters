package com.nextfaze.poweradapters;

import androidx.annotation.NonNull;

final class AdapterCondition extends Condition {

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            notifyChanged();
        }
    };

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final Predicate<PowerAdapter> mPredicate;

    AdapterCondition(@NonNull PowerAdapter adapter, @NonNull Predicate<PowerAdapter> predicate) {
        mPredicate = predicate;
        mAdapter = adapter;
    }

    @Override
    public boolean eval() {
        return mPredicate.apply(mAdapter);
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mAdapter.registerDataObserver(mDataObserver);
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mAdapter.unregisterDataObserver(mDataObserver);
    }
}
