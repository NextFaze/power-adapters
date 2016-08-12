package com.nextfaze.poweradapters.recyclerview;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecyclerConverterAdapter extends RecyclerView.Adapter<RecyclerConverterAdapter.ViewHolder> {

    @NonNull
    private final WeakMap<RecyclerView, RecyclerViewContainer> mContainers = new WeakMap<>();

    @NonNull
    private final Set<AdapterDataObserver> mAdapterDataObservers = new HashSet<>();

    @NonNull
    private final Set<RecyclerView> mAttachedRecyclerViews = new HashSet<>();

    @NonNull
    private final PowerAdapter mPowerAdapter;

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChanged() {
            mShadowItemCount = mPowerAdapter.getItemCount();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            validateItemCount();
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mShadowItemCount += itemCount;
            validateItemCount();
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mShadowItemCount -= itemCount;
            validateItemCount();
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            validateItemCount();
            if (itemCount == 1) {
                notifyItemMoved(fromPosition, toPosition);
            } else {
                // TODO: There's likely a more specific (series of?) calls we can make here instead of generic "changed" fallback.
                notifyDataSetChanged();
            }
        }
    };

    @NonNull
    private final Map<Object, Integer> mViewTypeObjectToInt = new ArrayMap<>();

    @NonNull
    private final Map<Integer, Object> mViewTypeIntToObject = new ArrayMap<>();

    private int mNextViewTypeInt;

    /** Used to track the expected number of items, based on incoming notifications. */
    private int mShadowItemCount;

    private boolean mObserving;

    public RecyclerConverterAdapter(@NonNull PowerAdapter powerAdapter) {
        mPowerAdapter = powerAdapter;
        super.setHasStableIds(mPowerAdapter.hasStableIds());
    }

    @Override
    public final void setHasStableIds(boolean hasStableIds) {
        throw new UnsupportedOperationException("setHasStableIds() is controlled by the wrapped PowerAdapter");
    }

    @Override
    public final int getItemCount() {
        return mPowerAdapter.getItemCount();
    }

    @Override
    public final long getItemId(int position) {
        return mPowerAdapter.getItemId(position);
    }

    @Override
    public final int getItemViewType(int position) {
        Object viewType = mPowerAdapter.getItemViewType(position);
        Integer viewTypeInt = mViewTypeObjectToInt.get(viewType);
        if (viewTypeInt == null) {
            viewTypeInt = mNextViewTypeInt++;
            mViewTypeObjectToInt.put(viewType, viewTypeInt);
            mViewTypeIntToObject.put(viewTypeInt, viewType);
        }
        return viewTypeInt;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int itemViewType) {
        return new ViewHolder(mPowerAdapter.newView(parent, mViewTypeIntToObject.get(itemViewType)));
    }

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position) {
        mPowerAdapter.bindView(holder.itemView, holder.holder);
    }

    @Override
    public final void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        mAdapterDataObservers.add(observer);
        updateObserver();
    }

    @Override
    public final void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        mAdapterDataObservers.remove(observer);
        updateObserver();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerViewContainer container = new RecyclerViewContainer(recyclerView);
        mContainers.put(recyclerView, container);
        container.onAdapterAttached();
        mPowerAdapter.onAttachedToContainer(container);
        updateObserver();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mAttachedRecyclerViews.remove(recyclerView);
        RecyclerViewContainer container = mContainers.get(recyclerView);
        if (container != null) {
            mPowerAdapter.onDetachedFromContainer(container);
            container.onAdapterDetached();
        }
        if (container == null) {
            // Should never happen, unless external caller invokes
            // onDetachedFromRecyclerView without a prior onAttachedToRecyclerView.
            throw new AssertionError();
        }
        updateObserver();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    private void updateObserver() {
        boolean observe = !mAdapterDataObservers.isEmpty() && !mAttachedRecyclerViews.isEmpty();
        if (observe != mObserving) {
            if (mObserving) {
                mPowerAdapter.unregisterDataObserver(mDataObserver);
                mShadowItemCount = 0;
            }
            mObserving = observe;
            if (mObserving) {
                mShadowItemCount = mPowerAdapter.getItemCount();
                notifyDataSetChanged();
                mPowerAdapter.registerDataObserver(mDataObserver);
            }
        }
    }

    private void notifyContainerAttachedToWindow(@NonNull Container container) {
        mPowerAdapter.onContainerAttachedToWindow(container);
    }

    private void notifyContainerDetachedFromWindow(@NonNull Container container) {
        mPowerAdapter.onContainerDetachedFromWindow(container);
    }

    /**
     * Check the item count by comparing with our shadow count. If they don't match, there's a good chance {@link
     * RecyclerView} will crash later on. By doing it aggressively ourselves, we can catch a poorly-behaved {@link
     * PowerAdapter} early.
     */
    private void validateItemCount() {
        int itemCount = mPowerAdapter.getItemCount();
        if (mShadowItemCount != itemCount) {
            throw new IllegalStateException("Inconsistency detected: expected item count " +
                    mShadowItemCount + " but it is " + itemCount);
        }
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        private final com.nextfaze.poweradapters.Holder holder = new com.nextfaze.poweradapters.Holder() {
            @Override
            public int getPosition() {
                return getLayoutPosition();
            }
        };

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    final class RecyclerViewContainer implements Container {

        @NonNull
        private final OnAttachStateChangeListener mOnAttachStateChangeListener = new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                notifyContainerAttachedToWindow(RecyclerViewContainer.this);
                updatePresenceInSet();
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                notifyContainerDetachedFromWindow(RecyclerViewContainer.this);
                updatePresenceInSet();
            }
        };

        @NonNull
        private final RecyclerView mRecyclerView;

        RecyclerViewContainer(@NonNull RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        void onAdapterAttached() {
            mRecyclerView.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
            updatePresenceInSet();
            if (mRecyclerView.isAttachedToWindow()) {
                notifyContainerAttachedToWindow(this);
            }
        }

        void onAdapterDetached() {
            mRecyclerView.removeOnAttachStateChangeListener(mOnAttachStateChangeListener);
            updatePresenceInSet();
            if (mRecyclerView.isAttachedToWindow()) {
                notifyContainerDetachedFromWindow(this);
            }
        }

        private void updatePresenceInSet() {
            if (mRecyclerView.isAttachedToWindow()) {
                mAttachedRecyclerViews.add(mRecyclerView);
            } else {
                mAttachedRecyclerViews.remove(mRecyclerView);
            }
            updateObserver();
        }
    }
}
