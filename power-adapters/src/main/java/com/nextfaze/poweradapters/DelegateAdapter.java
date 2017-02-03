package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

/** Wraps another {@link PowerAdapter} optionally, delegating work to it. The wrapped adapter can be reassigned freely. */
public final class DelegateAdapter extends PowerAdapter {

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    @Nullable
    private PowerAdapter mDelegate;

    @Nullable
    private PowerAdapter mObservedAdapter;

    public DelegateAdapter() {
    }

    public DelegateAdapter(@Nullable PowerAdapter delegate) {
        mDelegate = delegate;
    }

    public void setDelegate(@Nullable PowerAdapter delegate) {
        if (delegate != mDelegate) {
            int removeCount = mDelegate != null ? mDelegate.getItemCount() : 0;
            mDelegate = delegate;
            int insertCount = mDelegate != null ? mDelegate.getItemCount() : 0;
            updateObservers();
            if (removeCount > 0) {
                notifyItemRangeRemoved(0, removeCount);
            }
            if (insertCount > 0) {
                notifyItemRangeInserted(0, insertCount);
            }
        }
    }

    @Nullable
    public PowerAdapter getDelegate() {
        return mDelegate;
    }

    @Override
    public int getItemCount() {
        return mDelegate != null ? mDelegate.getItemCount() : 0;
    }

    @Override
    public boolean hasStableIds() {
        return mDelegate != null && mDelegate.hasStableIds();
    }

    @Override
    public long getItemId(int position) {
        return delegateOrThrow().getItemId(position);
    }

    @NonNull
    @Override
    public Object getItemViewType(int position) {
        return delegateOrThrow().getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return delegateOrThrow().isEnabled(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        return delegateOrThrow().newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull Container container, @NonNull View v, @NonNull Holder holder) {
        delegateOrThrow().bindView(container, v, holder);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        updateObservers();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        updateObservers();
    }

    private void updateObservers() {
        PowerAdapter adapter = getObserverCount() > 0 ? mDelegate : null;
        if (adapter != mObservedAdapter) {
            if (mObservedAdapter != null) {
                mObservedAdapter.unregisterDataObserver(mDataObserver);
            }
            mObservedAdapter = adapter;
            if (mObservedAdapter != null) {
                mObservedAdapter.registerDataObserver(mDataObserver);
            }
        }
    }

    @NonNull
    private PowerAdapter delegateOrThrow() {
        if (mDelegate == null) {
            throw new AssertionError();
        }
        return mDelegate;
    }
}
