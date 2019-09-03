package com.nextfaze.poweradapters.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public class RecyclerConverterAdapter extends RecyclerView.Adapter<RecyclerConverterAdapter.ViewHolder> {

    @NonNull
    private final WeakMap<RecyclerView, RecyclerViewContainer> mRecyclerViewToContainer = new WeakMap<>();

    @NonNull
    private final Set<AdapterDataObserver> mAdapterDataObservers = new HashSet<>();

    @NonNull
    final Set<RecyclerView> mAttachedRecyclerViews = new HashSet<>();

    @NonNull
    final PowerAdapter mPowerAdapter;

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            notifyItemRangeChanged(positionStart, itemCount, payload);
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

    private boolean mObserving;

    public RecyclerConverterAdapter(@NonNull PowerAdapter powerAdapter) {
        mPowerAdapter = checkNotNull(powerAdapter, "powerAdapter");
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int itemViewType) {
        View itemView = mPowerAdapter.newView(parent, mViewTypeIntToObject.get(itemViewType));
        return new ViewHolder(itemView, getContainer((RecyclerView) parent));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        onBindViewHolder(holder, position, Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        mPowerAdapter.bindView(holder.container, holder.itemView, holder.holder, payloads);
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
        RecyclerViewContainer container = getContainer(recyclerView);
        container.onAdapterAttached();
        updateObserver();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mAttachedRecyclerViews.remove(recyclerView);
        RecyclerViewContainer container = getContainerOrThrow(recyclerView);
        container.onAdapterDetached();
        updateObserver();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    void updateObserver() {
        boolean observe = !mAdapterDataObservers.isEmpty() && !mAttachedRecyclerViews.isEmpty();
        if (observe != mObserving) {
            if (mObserving) {
                mPowerAdapter.unregisterDataObserver(mDataObserver);
            }
            mObserving = observe;
            if (mObserving) {
                notifyDataSetChanged();
                mPowerAdapter.registerDataObserver(mDataObserver);
            }
        }
    }

    @NonNull
    private RecyclerViewContainer getContainer(@NonNull RecyclerView recyclerView) {
        RecyclerViewContainer container = mRecyclerViewToContainer.get(recyclerView);
        if (container == null) {
            container = new RecyclerViewContainer(recyclerView);
            mRecyclerViewToContainer.put(recyclerView, container);
        }
        return container;
    }

    @NonNull
    private RecyclerViewContainer getContainerOrThrow(@NonNull RecyclerView recyclerView) {
        RecyclerViewContainer container = mRecyclerViewToContainer.get(recyclerView);
        if (container == null) {
            // Should never happen, unless external caller invokes
            // onDetachedFromRecyclerView without a prior onAttachedToRecyclerView.
            throw new AssertionError();
        }
        return container;
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        final com.nextfaze.poweradapters.Holder holder = new com.nextfaze.poweradapters.Holder() {
            @Override
            public int getPosition() {
                return getLayoutPosition();
            }
        };

        @NonNull
        final RecyclerViewContainer container;

        ViewHolder(View itemView, @NonNull RecyclerViewContainer container) {
            super(itemView);
            this.container = container;
        }
    }

    private final class RecyclerViewContainer extends Container {

        @NonNull
        private final OnAttachStateChangeListener mOnAttachStateChangeListener = new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                updatePresenceInSet();
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                updatePresenceInSet();
            }
        };

        @NonNull
        private final RecyclerView mRecyclerView;

        RecyclerViewContainer(@NonNull RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        @Override
        public void scrollToPosition(int position) {
            mRecyclerView.smoothScrollToPosition(position);
        }

        @Override
        public int getItemCount() {
            return mPowerAdapter.getItemCount();
        }

        @NonNull
        @Override
        public ViewGroup getViewGroup() {
            return mRecyclerView;
        }

        @NonNull
        @Override
        public Container getRootContainer() {
            return this;
        }

        void onAdapterAttached() {
            mRecyclerView.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
            updatePresenceInSet();
        }

        void onAdapterDetached() {
            mRecyclerView.removeOnAttachStateChangeListener(mOnAttachStateChangeListener);
            updatePresenceInSet();
        }

        void updatePresenceInSet() {
            if (mRecyclerView.isAttachedToWindow()) {
                mAttachedRecyclerViews.add(mRecyclerView);
            } else {
                mAttachedRecyclerViews.remove(mRecyclerView);
            }
            updateObserver();
        }
    }
}
