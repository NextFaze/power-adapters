package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.WeakHashMap;

public class PowerAdapterWrapper extends AbstractPowerAdapter {

    @NonNull
    private final WeakHashMap<View, HolderWrapper> mHolders = new WeakHashMap<>();

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final DataObserver mDataSetObserver = new DataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(innerToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(innerToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(innerToOuter(positionStart), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemRangeMoved(innerToOuter(fromPosition), innerToOuter(toPosition), itemCount);
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
     * @see #outerToInner(int)
     */
    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(outerToInner(position));
    }

    /**
     * Forwards the call to the wrapped adapter, converting the {@code position} value to the wrapped adapter's
     * coordinate space.
     * @see #outerToInner(int)
     */
    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(outerToInner(position));
    }

    /**
     * Forwards the call to the wrapped adapter, converting the {@code position} value to the wrapped adapter's
     * coordinate space.
     * @see #outerToInner(int)
     */
    @Override
    public boolean isEnabled(int position) {
        return mAdapter.isEnabled(outerToInner(position));
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
                    return outerToInner(super.getPosition());
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
     * correct. This method is also called when forwarding calls that accept a {@code position} parameter.
     * @param outerPosition The {@code position} in this adapter's coordinate space.
     * @return The {@code position} converted into the coordinate space of the wrapped adapter.
     */
    protected int outerToInner(int outerPosition) {
        return outerPosition;
    }

    /**
     * Converts a {@code position} in the wrapped adapter's coordinate space to the coordinate space of this adapter.
     * By default, simply returns returns the position value unchanged. Must be overridden by subclasses that augment
     * the items in this adapter, otherwise fine-grained change notifications emitted by the wrapped adapter will not
     * match the coordinate space of this adapter.
     * @param innerPosition The {@code position} in the wrapped adapter's coordinate space.
     * @return The {@code position} converted into the coordinate space of this adapter.
     */
    protected int innerToOuter(int innerPosition) {
        return innerPosition;
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mAdapter.registerDataObserver(mDataSetObserver);
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mAdapter.unregisterDataObserver(mDataSetObserver);
    }
}
