package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.internal.WeakMap;

import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public class PowerAdapterWrapper extends PowerAdapter {

    @NonNull
    private final WeakMap<Holder, HolderWrapper> mHolders = new WeakMap<>();

    @NonNull
    private final WeakMap<Container, ContainerWrapper> mContainers = new WeakMap<>();

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final DataObserver mDataSetObserver = new DataObserver() {
        @Override
        public void onChanged() {
            forwardChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            forwardItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            forwardItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            forwardItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            forwardItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    public PowerAdapterWrapper(@NonNull PowerAdapter adapter) {
        mAdapter = checkNotNull(adapter, "adapter");
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
    @NonNull
    @Override
    public Object getItemViewType(int position) {
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
    public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        return mAdapter.newView(parent, viewType);
    }

    @Override
    public void bindView(
            @NonNull Container container,
            @NonNull View view,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        HolderWrapper holderWrapper = mHolders.get(holder);
        if (holderWrapper == null) {
            holderWrapper = new HolderWrapper(holder) {
                @Override
                public int getPosition() {
                    return outerToInner(super.getPosition());
                }
            };
            mHolders.put(holder, holderWrapper);
        }
        ContainerWrapper containerWrapper = mContainers.get(container);
        if (containerWrapper == null) {
            containerWrapper = new ContainerWrapper(container) {
                @Override
                public void scrollToPosition(int position) {
                    super.scrollToPosition(innerToOuter(position));
                }

                @Override
                public int getItemCount() {
                    return PowerAdapterWrapper.this.getItemCount();
                }
            };
            mContainers.put(container, containerWrapper);
        }
        mAdapter.bindView(containerWrapper, view, holderWrapper, payloads);
    }

    /**
     * Converts a {@code position} in this adapter's coordinate space to the coordinate space of the wrapped adapter.
     * By default, simply returns returns the position value unchanged. Must be overridden by subclasses that augment
     * the items in this adapter, in order for the {@link PowerAdapter#bindView(Container, View, Holder, List)} {@link Holder} position to be
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

    protected void forwardChanged() {
        notifyDataSetChanged();
    }

    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount, @Nullable Object payload) {
        notifyItemRangeChanged(innerToOuter(innerPositionStart), innerItemCount, payload);
    }

    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        notifyItemRangeInserted(innerToOuter(innerPositionStart), innerItemCount);
    }

    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        notifyItemRangeRemoved(innerToOuter(innerPositionStart), innerItemCount);
    }

    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        notifyItemRangeMoved(innerToOuter(innerFromPosition), innerToOuter(innerToPosition), innerItemCount);
    }
}
