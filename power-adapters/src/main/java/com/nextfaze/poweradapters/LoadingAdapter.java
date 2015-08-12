package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.DataObserver;
import com.nextfaze.asyncdata.LoadingObserver;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

/**
 * Wraps an existing {@link PowerAdapter} and displays a loading indicator while loading. Also supports checking a
 * {@link Data} instance for the loading state. The loading indicator is shown at the end of the adapter.
 */
@Accessors(prefix = "m")
public abstract class LoadingAdapter extends PowerAdapterWrapper {

    protected LoadingAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    /**
     * Override this to indicate the current loading state. Invoke {@link #notifyLoadingChanged()} if the state
     * changes.
     * @return {@code true} if currently loading, otherwise {@code false}.
     */
    protected abstract boolean isLoading();

    /**
     * Override this to indicate if the loading item should be shown in the current state. By default returns {@link
     * #isLoading()}.
     * @return {@code true} to show the loading item in the current state.
     */
    protected boolean isLoadingItemVisible() {
        return isLoading();
    }

    /**
     * Determines whether the loading item should be enabled in the list, allowing it to be clicked or not.
     * <p/>
     * Returns {@code false} by default.
     * @return {@code true} if the loading item should be enabled, otherwise {@code false}.
     * @see ListAdapter#isEnabled(int)
     */
    protected boolean isLoadingItemEnabled() {
        return false;
    }

    /** Call this to notify the loading adapter that the value of {@link #isLoading()} has changed. */
    protected final void notifyLoadingChanged() {
        notifyDataSetChanged();
    }

    @Override
    public final int getItemCount() {
        if (isLoadingItemVisible()) {
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
            return isLoadingItemEnabled();
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
    protected int mapPosition(int outerPosition) {
        // No translation necessary for loading adapter, because the item appears at the end.
        return outerPosition;
    }

    private int loadingViewType() {
        return super.getViewTypeCount();
    }

    @NonNull
    protected abstract View newLoadingView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent);

    private boolean isLoadingItem(int position) {
        if (!isLoadingItemVisible()) {
            return false;
        }
        // Loading item is the last item in the list.
        return position == getItemCount() - 1;
    }

    /** Determines when the loading item is shown while empty. Item is never shown if not loading. */
    public enum EmptyPolicy {
        /** Show the loading item ONLY while empty. */
        SHOW_ONLY_IF_EMPTY {
            @Override
            boolean shouldShow(@NonNull LoadingAdapter adapter) {
                return adapter.isLoading() && adapter.getAdapter().getItemCount() == 0;
            }
        },
        /** Show the loading item regardless of the empty state. */
        SHOW_ALWAYS {
            @Override
            boolean shouldShow(@NonNull LoadingAdapter adapter) {
                return adapter.isLoading();
            }
        };

        abstract boolean shouldShow(@NonNull LoadingAdapter adapter);
    }

    public static final class Builder {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final Data<?> mData;

        @Nullable
        private Item mLoadingItem;

        @NonNull
        private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW_ALWAYS;

        private boolean mLoadingItemEnabled;

        public Builder(@NonNull PowerAdapter adapter, @NonNull Data<?> data) {
            mAdapter = adapter;
            mData = data;
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

        /** @see #isLoadingItemEnabled() */
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
            return new Impl(mAdapter, mData, mLoadingItem, mEmptyPolicy, mLoadingItemEnabled);
        }
    }

    private static final class Impl extends LoadingAdapter {

        @NonNull
        private final Data<?> mData;

        @NonNull
        private final DataObserver mDataObserver = new DataObserver() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        };

        @NonNull
        private final LoadingObserver mLoadingObserver = new LoadingObserver() {
            @Override
            public void onLoadingChange() {
                notifyLoadingChanged();
            }
        };

        @NonNull
        private final Item mLoadingItem;

        @NonNull
        private final EmptyPolicy mEmptyPolicy;

        private final boolean mLoadingItemEnabled;

        Impl(@NonNull PowerAdapter adapter,
             @NonNull Data<?> data,
             @NonNull Item loadingItem,
             @NonNull EmptyPolicy emptyPolicy,
             boolean loadingItemEnabled) {
            super(adapter);
            mData = data;
            mLoadingItem = loadingItem;
            mLoadingItemEnabled = loadingItemEnabled;
            mEmptyPolicy = emptyPolicy;
        }

        @Override
        protected void onFirstObserverRegistered() {
            mData.registerDataObserver(mDataObserver);
            mData.registerLoadingObserver(mLoadingObserver);
        }

        @Override
        protected void onLastObserverUnregistered() {
            mData.unregisterDataObserver(mDataObserver);
            mData.unregisterLoadingObserver(mLoadingObserver);
        }

        @Override
        protected boolean isLoading() {
            return mData.isLoading();
        }

        @NonNull
        @Override
        protected View newLoadingView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
            return mLoadingItem.get(layoutInflater, parent);
        }

        @Override
        protected boolean isLoadingItemVisible() {
            return mEmptyPolicy.shouldShow(this);
        }

        @Override
        protected boolean isLoadingItemEnabled() {
            return mLoadingItemEnabled;
        }
    }
}