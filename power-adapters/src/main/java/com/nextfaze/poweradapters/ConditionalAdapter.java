package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

final class ConditionalAdapter extends PowerAdapter {

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

    @NonNull
    private final Observer mObserver = new Observer() {
        @Override
        public void onChanged() {
            updateVisible();
        }
    };

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final Condition mCondition;

    private boolean mVisible;

    private boolean mObservingAdapter;

    ConditionalAdapter(@NonNull PowerAdapter adapter, @NonNull Condition condition) {
        mAdapter = adapter;
        mCondition = condition;
    }

    @Override
    public int getItemCount() {
        return mVisible ? mAdapter.getItemCount() : 0;
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public long getItemId(int position) {
        return adapter().getItemId(position);
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        return adapter().getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return adapter().isEnabled(position);
    }

    @NonNull
    private PowerAdapter adapter() {
        if (!mVisible) {
            throw new AssertionError();
        }
        return mAdapter;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return adapter().newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View v, @NonNull Holder holder) {
        adapter().bindView(v, holder);
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mCondition.registerObserver(mObserver);
        boolean visible = mCondition.eval();
        if (visible != mVisible) {
            int removeCount = mVisible ? mAdapter.getItemCount() : 0;
            mVisible = visible;
            int insertCount = mVisible ? mAdapter.getItemCount() : 0;
            if (removeCount > 0) {
                notifyItemRangeRemoved(0, removeCount);
            }
            if (insertCount > 0) {
                notifyItemRangeInserted(0, insertCount);
            }
            if (mVisible != mObservingAdapter) {
                mObservingAdapter = mVisible;
                if (mObservingAdapter) {
                    mAdapter.registerDataObserver(mDataObserver);
                } else {
                    mAdapter.unregisterDataObserver(mDataObserver);
                }
            }
        }
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        if (mObservingAdapter) {
            mObservingAdapter = false;
            mAdapter.unregisterDataObserver(mDataObserver);
        }
        mCondition.unregisterObserver(mObserver);
    }

    private void updateVisible() {
        boolean visible = mCondition.eval();
        if (visible != mVisible) {
            int removeCount = mVisible ? mAdapter.getItemCount() : 0;
            mVisible = visible;
            int insertCount = mVisible ? mAdapter.getItemCount() : 0;
            if (removeCount > 0) {
                notifyItemRangeRemoved(0, removeCount);
            }
            if (insertCount > 0) {
                notifyItemRangeInserted(0, insertCount);
            }
            boolean observe = mVisible && getObserverCount() > 0;
            if (observe != mObservingAdapter) {
                mObservingAdapter = observe;
                if (mObservingAdapter) {
                    mAdapter.registerDataObserver(mDataObserver);
                } else {
                    mAdapter.unregisterDataObserver(mDataObserver);
                }
            }
        }
    }
}
