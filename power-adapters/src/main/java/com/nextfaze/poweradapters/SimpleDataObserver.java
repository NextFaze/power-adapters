package com.nextfaze.poweradapters;

/** Forwards fine-grained calls to {@link DataObserver#onChanged()} by default. */
public class SimpleDataObserver implements DataObserver {
    @Override
    public void onChanged() {
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
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
