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

    private boolean mObservingData;

    private boolean mObservingCondition;

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
        updateDataObserver();
        updateConditionObserver();
        updateVisible();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        updateDataObserver();
        updateConditionObserver();
    }

    private void updateVisible() {
        boolean visible = mCondition.eval();
        if (visible != mVisible) {
            int removeCount = mVisible ? mAdapter.getItemCount() : 0;
            mVisible = visible;
            int insertCount = mVisible ? mAdapter.getItemCount() : 0;
            updateDataObserver();
            if (removeCount > 0) {
                notifyItemRangeRemoved(0, removeCount);
            }
            if (insertCount > 0) {
                notifyItemRangeInserted(0, insertCount);
            }
        }
    }

    private void updateDataObserver() {
        // Only observe wrapped adapter if:
        // - Our own clients are observing us
        // - The adapter is currently visible according to the condition
        boolean observe = mVisible && getObserverCount() > 0;
        if (observe != mObservingData) {
            mObservingData = observe;
            if (mObservingData) {
                mAdapter.registerDataObserver(mDataObserver);
            } else {
                mAdapter.unregisterDataObserver(mDataObserver);
            }
        }
    }

    private void updateConditionObserver() {
        // Only observe condition if:
        // - Our own clients are observing us
        boolean observe = getObserverCount() > 0;
        if (observe != mObservingCondition) {
            mObservingCondition = observe;
            if (mObservingCondition) {
                mCondition.registerObserver(mObserver);
            } else {
                mCondition.unregisterObserver(mObserver);
            }
        }
    }
}
