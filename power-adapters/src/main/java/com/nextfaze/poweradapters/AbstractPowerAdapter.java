package com.nextfaze.poweradapters;

import lombok.NonNull;

public abstract class AbstractPowerAdapter implements PowerAdapter {

    @NonNull
    private final DataObservable mDataObservable = new DataObservable();

    @Override
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        boolean firstAdded;
        synchronized (mDataObservable) {
            mDataObservable.registerObserver(dataObserver);
            firstAdded = mDataObservable.size() == 1;
        }
        if (firstAdded) {
            onFirstObserverRegistered();
        }
    }

    @Override
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        boolean lastRemoved;
        synchronized (mDataObservable) {
            mDataObservable.unregisterObserver(dataObserver);
            lastRemoved = mDataObservable.size() == 0;
        }
        if (lastRemoved) {
            onLastObserverUnregistered();
        }
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return NO_ID;
    }

    @NonNull
    @Override
    public Metadata getItemMetadata(int position) {
        return Metadata.NONE;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    public final void notifyDataSetChanged() {
        mDataObservable.notifyDataSetChanged();
    }

    public final void notifyItemChanged(int position) {
        mDataObservable.notifyItemChanged(position);
    }

    public final void notifyItemRangeChanged(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeChanged(positionStart, itemCount);
    }

    public final void notifyItemInserted(int position) {
        mDataObservable.notifyItemInserted(position);
    }

    public final void notifyItemRangeInserted(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeInserted(positionStart, itemCount);
    }

    public final void notifyItemMoved(int fromPosition, int toPosition) {
        mDataObservable.notifyItemMoved(fromPosition, toPosition);
    }

    public final void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        mDataObservable.notifyItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    public final void notifyItemRemoved(int position) {
        mDataObservable.notifyItemRemoved(position);
    }

    public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeRemoved(positionStart, itemCount);
    }

    /** Called when the first observer has registered with this adapter. Subclasses must call through to super. */
    protected void onFirstObserverRegistered() {
    }

    /** Called when the last observer has unregistered from this adapter. Subclasses must call through to super. */
    protected void onLastObserverUnregistered() {
    }
}
