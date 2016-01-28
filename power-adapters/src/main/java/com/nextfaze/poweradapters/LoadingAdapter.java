package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
final class LoadingAdapter extends ItemAdapter {

    @NonNull
    private LoadingAdapterBuilder.Delegate mDelegate;

    @NonNull
    private LoadingAdapterBuilder.EmptyPolicy mEmptyPolicy;

    LoadingAdapter(@NonNull LoadingAdapterBuilder.Delegate delegate,
                   @NonNull Item item,
                   @NonNull LoadingAdapterBuilder.EmptyPolicy emptyPolicy) {
        super(item);
        mEmptyPolicy = emptyPolicy;
        mDelegate = delegate;
        mDelegate.setAdapter(this);
        updateVisible();
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mDelegate.onFirstObserverRegistered();
        updateVisible();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mDelegate.onLastObserverUnregistered();
        updateVisible();
    }

    void notifyEmptyChanged() {
        updateVisible();
    }

    void notifyLoadingChanged() {
        updateVisible();
    }

    private void updateVisible() {
        setAllVisible(mDelegate.isLoading() && mEmptyPolicy.shouldShow(mDelegate));
    }
}