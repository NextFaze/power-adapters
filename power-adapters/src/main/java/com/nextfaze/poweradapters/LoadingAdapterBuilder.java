package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Adapter;
import lombok.NonNull;

/** Wraps an existing {@link PowerAdapter} and displays a loading indicator while loading. */
public final class LoadingAdapterBuilder {

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final Delegate mDelegate;

    @Nullable
    private Item mLoadingItem;

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW_ALWAYS;

    private boolean mLoadingItemEnabled;

    public LoadingAdapterBuilder(@NonNull PowerAdapter adapter, @NonNull Delegate delegate) {
        mAdapter = adapter;
        mDelegate = delegate;
    }

    @NonNull
    public LoadingAdapterBuilder loadingItemResource(@LayoutRes int loadingItemResource) {
        mLoadingItem = new Item(loadingItemResource);
        return this;
    }

    @NonNull
    public LoadingAdapterBuilder loadingItemView(@NonNull View loadingItemView) {
        mLoadingItem = new Item(loadingItemView);
        return this;
    }

    @NonNull
    public LoadingAdapterBuilder loadingItemEnabled(boolean loadingItemEnabled) {
        mLoadingItemEnabled = loadingItemEnabled;
        return this;
    }

    /** If {@code true}, loading item is only shown while {@link Adapter#isEmpty()} is {@code true}. */
    @NonNull
    public LoadingAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
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

    /** Invoked by {@link LoadingAdapter} to determine when the loading item is shown. */
    public static abstract class Delegate {

        @Nullable
        private LoadingAdapter mAdapter;

        /**
         * Returns whether the loading item should be shown or not.
         * Invoke {@link #notifyLoadingChanged()} to inform the owning adapter if the empty state has changed.
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

        void setAdapter(@Nullable LoadingAdapter adapter) {
            mAdapter = adapter;
        }
    }
}
