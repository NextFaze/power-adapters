package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
final class EmptyAdapter extends ItemAdapter {

    @NonNull
    private final EmptyAdapterBuilder.Delegate mDelegate;

    @NonNull
    private final Item mItem;

    EmptyAdapter(@NonNull EmptyAdapterBuilder.Delegate delegate, @NonNull Item item) {
        super(item);
        mItem = item;
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

    void updateVisible() {
        setAllVisible(mDelegate.isEmpty());
    }
}
