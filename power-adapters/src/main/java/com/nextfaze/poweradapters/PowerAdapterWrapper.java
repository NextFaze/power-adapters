package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class PowerAdapterWrapper extends AbstractPowerAdapter {

    @NonNull
    private final WeakHashMap<View, HolderWrapper> mHolders = new WeakHashMap<>();

    @NonNull
    private final Set<DataObserver> mDataSetObservers = new CopyOnWriteArraySet<>();

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final DataObserver mDataSetObserver = new DataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        // TODO: Map the following positions.

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

    public PowerAdapterWrapper(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
    }

    @NonNull
    public final PowerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    /**
     * Forwards the call to the wrapped adapter, converting the {@code position} value to the wrapped adapter's
     * coordinate space.
     * @see #mapPosition(int)
     */
    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(mapPosition(position));
    }

    /**
     * Forwards the call to the wrapped adapter, converting the {@code position} value to the wrapped adapter's
     * coordinate space.
     * @see #mapPosition(int)
     */
    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(mapPosition(position));
    }

    @Override
    @NonNull
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        return mAdapter.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        HolderWrapper holderWrapper = mHolders.get(view);
        if (holderWrapper == null) {
            holderWrapper = new HolderWrapper(holder) {
                @Override
                public int getPosition() {
                    return mapPosition(super.getPosition());
                }
            };
            mHolders.put(view, holderWrapper);
        }
        mAdapter.bindView(view, holderWrapper);
    }

    /**
     * Converts a {@code position} in this adapter's coordinate space to the coordinate space of the wrapped adapter.
     * By default, simply returns returns the position value unchanged. Must be overridden by subclasses that augment
     * the items in this adapter, in order for the {@link #bindView(View, Holder)} {@link Holder} position to be
     * correct.
     * @param outerPosition The {@code position} in this adapter's coordinate space.
     * @return The {@code position} converted into the coordinate space of the wrapped adapter.
     */
    protected int mapPosition(int outerPosition) {
        return outerPosition;
    }
}
