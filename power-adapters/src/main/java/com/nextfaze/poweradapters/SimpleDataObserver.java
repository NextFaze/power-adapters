package com.nextfaze.poweradapters;

import androidx.annotation.Nullable;

/** Forwards fine-grained calls to {@link DataObserver#onChanged()} by default. */
public abstract class SimpleDataObserver implements DataObserver {
    @Override
    public abstract void onChanged();

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        onChanged();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        onChanged();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        onChanged();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        onChanged();
    }
}
