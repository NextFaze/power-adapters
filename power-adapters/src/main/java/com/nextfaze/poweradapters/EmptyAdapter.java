package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
public final class EmptyAdapter extends PowerAdapterWrapper {

    // TODO: Consolidate with LoadingAdapter, because both add a single item at the end.

    @NonNull
    private final Delegate mDelegate;

    @NonNull
    private final Item mEmptyItem;

    private final boolean mEmptyItemEnabled;

    private boolean mVisible;

    private EmptyAdapter(@NonNull PowerAdapter adapter,
                         @NonNull Delegate delegate,
                         @NonNull Item emptyItem,
                         boolean emptyItemEnabled) {
        super(adapter);
        mEmptyItem = emptyItem;
        mDelegate = delegate;
        mDelegate.mAdapter = this;
        mEmptyItemEnabled = emptyItemEnabled;
        updateVisible();
    }

    @CallSuper
    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mDelegate.onFirstObserverRegistered();
    }

    @CallSuper
    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mDelegate.onLastObserverUnregistered();
    }

    protected final void notifyEmptyChanged() {
        updateVisible();
    }

    private void updateVisible() {
        boolean visible = mDelegate.isEmpty();
        if (visible != mVisible) {
            mVisible = visible;
            if (visible) {
                notifyItemInserted(super.getItemCount());
            } else {
                notifyItemRemoved(super.getItemCount());
            }
        }
    }

    @Override
    public final int getItemCount() {
        if (mVisible) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    @Override
    public final int getViewTypeCount() {
        // Fixed amount of view types: whatever the underlying adapter wants, plus our empty item.
        return super.getViewTypeCount() + 1;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isEmptyItem(position)) {
            return emptyViewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public final boolean isEnabled(int position) {
        if (isEmptyItem(position)) {
            return mEmptyItemEnabled;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, int itemViewType) {
        if (itemViewType == emptyViewType()) {
            return newEmptyView(layoutInflater(parent), parent);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public final void bindView(@NonNull View view, @NonNull Holder holder) {
        if (!isEmptyItem(holder.getPosition())) {
            super.bindView(view, holder);
        }
    }

    @Override
    protected final int outerToInner(int outerPosition) {
        // No conversion necessary. The empty item is added at the end.
        return outerPosition;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        // No conversion necessary. The empty item is added at the end.
        return innerPosition;
    }

    @NonNull
    protected View newEmptyView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return mEmptyItem.get(layoutInflater, parent);
    }

    private boolean isEmptyItem(int position) {
        //noinspection SimplifiableIfStatement
        if (!mVisible) {
            return false;
        }
        return position == getItemCount() - 1;
    }

    private int emptyViewType() {
        return super.getViewTypeCount();
    }

    public static final class Builder {

        @NonNull
        private final Delegate mDelegate;

        @NonNull
        private final PowerAdapter mAdapter;

        @Nullable
        private Item mEmptyItem;

        private boolean mEmptyItemEnabled;

        public Builder(@NonNull PowerAdapter adapter) {
            this(adapter, new DefaultDelegate(adapter));
        }

        public Builder(@NonNull PowerAdapter adapter, @NonNull Delegate delegate) {
            mDelegate = delegate;
            mAdapter = adapter;
        }

        @NonNull
        public Builder emptyItemResource(@LayoutRes int emptyItemResource) {
            mEmptyItem = new Item(emptyItemResource);
            return this;
        }

        @NonNull
        public Builder emptyItemView(@NonNull View emptyItemView) {
            mEmptyItem = new Item(emptyItemView);
            return this;
        }

        /**
         * Sets whether the empty item should be enabled in the list, allowing it to be clicked or not.
         * @param emptyItemEnabled {@code true} to make it enabled, otherwise {@code false} to make it disabled.
         * @see ListAdapter#isEnabled(int)
         */
        @NonNull
        public Builder emptyItemEnabled(boolean emptyItemEnabled) {
            mEmptyItemEnabled = emptyItemEnabled;
            return this;
        }

        @NonNull
        public EmptyAdapter build() {
            if (mEmptyItem == null) {
                throw new IllegalStateException("No empty item specified");
            }
            return new EmptyAdapter(mAdapter, mDelegate, mEmptyItem, mEmptyItemEnabled);
        }
    }

    /** Invoked by {@link EmptyAdapter} to determine when the empty item is shown. */
    public static abstract class Delegate {

        @Nullable
        private EmptyAdapter mAdapter;

        /**
         * Determines if the empty item should currently be shown.
         * Invoke {@link #notifyEmptyChanged()} to inform the owning adapter if the empty state has changed.
         * @return {@code true} if the empty item should be shown right now, otherwise {@code false}.
         * @see #notifyEmptyChanged()
         */
        protected abstract boolean isEmpty();

        /**
         * @see PowerAdapterWrapper#onFirstObserverRegistered()
         */
        @UiThread
        protected void onFirstObserverRegistered() {
        }

        /**
         * @see PowerAdapterWrapper#onLastObserverUnregistered()
         */
        @UiThread
        protected void onLastObserverUnregistered() {
        }

        /** Must be called when the value of {@link #isEmpty()} changes. */
        @UiThread
        protected final void notifyEmptyChanged() {
            if (mAdapter != null) {
                mAdapter.updateVisible();
            }
        }
    }

    /** Empty state is determined by wrapped adapter size. */
    private static class DefaultDelegate extends Delegate {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final DataObserver mDataObserver = new SimpleDataObserver() {
            @Override
            public void onChanged() {
                notifyEmptyChanged();
            }
        };

        DefaultDelegate(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        protected boolean isEmpty() {
            return mAdapter.getItemCount() == 0;
        }

        @Override
        protected void onFirstObserverRegistered() {
            super.onFirstObserverRegistered();
            mAdapter.registerDataObserver(mDataObserver);
        }

        @Override
        protected void onLastObserverUnregistered() {
            super.onLastObserverUnregistered();
            mAdapter.unregisterDataObserver(mDataObserver);
        }
    }
}
