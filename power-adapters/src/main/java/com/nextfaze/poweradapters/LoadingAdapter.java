package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

/** Wraps an existing {@link PowerAdapter} and displays a loading indicator while loading. */
@Accessors(prefix = "m")
public final class LoadingAdapter extends PowerAdapterWrapper {

    @NonNull
    private final Delegate mDelegate;

    @NonNull
    private final Item mLoadingItem;

    @NonNull
    private final EmptyPolicy mEmptyPolicy;

    private boolean mVisible;

    private final boolean mLoadingItemEnabled;

    private LoadingAdapter(@NonNull PowerAdapter adapter,
                           @NonNull Item loadingItem,
                           @NonNull EmptyPolicy emptyPolicy,
                           @NonNull Delegate delegate,
                           boolean loadingItemEnabled) {
        super(adapter);
        mLoadingItem = loadingItem;
        mEmptyPolicy = emptyPolicy;
        mDelegate = delegate;
        mDelegate.mAdapter = this;
        mLoadingItemEnabled = loadingItemEnabled;
        updateVisible();
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
        // Fixed amount of view types: whatever the underlying adapter wants, plus our loading item.
        return super.getViewTypeCount() + 1;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isLoadingItem(position)) {
            return loadingViewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public final long getItemId(int position) {
        if (isLoadingItem(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
    }

    @Override
    public final boolean isEnabled(int position) {
        if (isLoadingItem(position)) {
            return mLoadingItemEnabled;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, int itemViewType) {
        if (itemViewType == loadingViewType()) {
            return newLoadingView(layoutInflater(parent), parent);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public final void bindView(@NonNull View view, @NonNull Holder holder) {
        if (!isLoadingItem(holder.getPosition())) {
            super.bindView(view, holder);
        }
    }

    @Override
    protected int outerToInner(int outerPosition) {
        // No translation necessary for loading adapter, because the item appears at the end.
        return outerPosition;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        // No translation necessary for loading adapter, because the item appears at the end.
        return super.innerToOuter(innerPosition);
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mDelegate.onFirstObserverRegistered();
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mDelegate.onLastObserverUnregistered();
    }

    private void updateVisible() {
        boolean visible = mDelegate.isLoading() && mEmptyPolicy.shouldShow(this);
        if (visible != mVisible) {
            mVisible = visible;
            if (visible) {
                notifyItemInserted(super.getItemCount());
            } else {
                notifyItemRemoved(super.getItemCount());
            }
        }
    }

    private int loadingViewType() {
        return super.getViewTypeCount();
    }

    private boolean isLoadingItem(int position) {
        if (!mVisible) {
            return false;
        }
        // Loading item is the last item in the list.
        return position == getItemCount() - 1;
    }

    @NonNull
    private View newLoadingView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return mLoadingItem.get(layoutInflater, parent);
    }

    /** Determines when the loading item is shown while empty. Item is never shown if not loading. */
    public enum EmptyPolicy {
        /** Show the loading item ONLY while wrapped adapter is empty. */
        SHOW_ONLY_IF_EMPTY {
            @Override
            boolean shouldShow(@NonNull LoadingAdapter adapter) {
                return adapter.getAdapter().getItemCount() == 0;
            }
        },
        /** Show the loading item regardless of the empty state. */
        SHOW_ALWAYS {
            @Override
            boolean shouldShow(@NonNull LoadingAdapter adapter) {
                return true;
            }
        };

        abstract boolean shouldShow(@NonNull LoadingAdapter adapter);
    }

    public static final class Builder {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final Delegate mDelegate;

        @Nullable
        private Item mLoadingItem;

        @NonNull
        private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW_ALWAYS;

        private boolean mLoadingItemEnabled;

        public Builder(@NonNull PowerAdapter adapter, @NonNull Delegate delegate) {
            mAdapter = adapter;
            mDelegate = delegate;
        }

        @NonNull
        public Builder loadingItemResource(@LayoutRes int loadingItemResource) {
            mLoadingItem = new Item(loadingItemResource);
            return this;
        }

        @NonNull
        public Builder loadingItemView(@NonNull View loadingItemView) {
            mLoadingItem = new Item(loadingItemView);
            return this;
        }

        @NonNull
        public Builder loadingItemEnabled(boolean loadingItemEnabled) {
            mLoadingItemEnabled = loadingItemEnabled;
            return this;
        }

        /** If {@code true}, loading item is only shown while {@link Adapter#isEmpty()} is {@code true}. */
        @NonNull
        public Builder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
            mEmptyPolicy = emptyPolicy;
            return this;
        }

        @NonNull
        public LoadingAdapter build() {
            if (mLoadingItem == null) {
                throw new IllegalStateException("No loading item specified");
            }
            return new LoadingAdapter(mAdapter, mLoadingItem, mEmptyPolicy, mDelegate, mLoadingItemEnabled);
        }
    }

    public static abstract class Delegate {

        @Nullable
        private LoadingAdapter mAdapter;

        /**
         * Returns whether the loading item should be shown or not.
         * @return {@code true} if the item should be shown, otherwise {@code false}.
         * @see #notifyLoadingChanged()
         */
        @UiThread
        protected abstract boolean isLoading();

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

        /** Must be called when the value of {@link #isLoading()} changes. */
        @UiThread
        protected final void notifyLoadingChanged() {
            if (mAdapter != null) {
                mAdapter.updateVisible();
            }
        }
    }

}