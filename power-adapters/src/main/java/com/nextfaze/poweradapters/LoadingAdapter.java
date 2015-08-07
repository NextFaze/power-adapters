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

import static com.nextfaze.poweradapters.AdapterUtils.layoutInflater;

/**
 * Wraps an existing {@link ListAdapter} and displays a loading indicator while loading. Also supports checking a
 * {@link Data} instance for the loading state. The loading indicator is shown at the end of the adapter.
 */
@Accessors(prefix = "m")
public abstract class LoadingAdapter extends ListAdapterWrapper {

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter) */
    protected LoadingAdapter(@NonNull ListAdapter adapter) {
        super(adapter);
    }

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
    protected LoadingAdapter(@NonNull ListAdapter adapter, boolean takeOwnership) {
        super(adapter, takeOwnership);
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
    public final int getCount() {
        if (isLoadingItemVisible()) {
            return mAdapter.getCount() + 1;
        }
        return mAdapter.getCount();
    }

    @Override
    public final int getViewTypeCount() {
        // Fixed amount of view types: whatever the underlying adapter wants, plus our loading item.
        return super.getViewTypeCount() + 1;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isLoadingItemVisible() && isLoadingItem(position)) {
            return getLoadingViewType();
        }
        return super.getItemViewType(outerToInnerPosition(position));
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == getLoadingViewType()) {
            if (convertView == null) {
                convertView = newLoadingView(layoutInflater(parent), position, parent);
            }
            return convertView;
        }
        return super.getView(outerToInnerPosition(position), convertView, parent);
    }

    @Override
    public final boolean isEnabled(int position) {
        if (isLoadingItemVisible() && isLoadingItem(position)) {
            return isLoadingItemEnabled();
        }
        return super.isEnabled(outerToInnerPosition(position));
    }

    @Override
    public final long getItemId(int position) {
        if (isLoadingItemVisible() && isLoadingItem(position)) {
            return -1;
        }
        return super.getItemId(outerToInnerPosition(position));
    }

    @Override
    public final Object getItem(int position) {
        if (isLoadingItemVisible() && isLoadingItem(position)) {
            return null;
        }
        return super.getItem(outerToInnerPosition(position));
    }

    private int getLoadingViewType() {
        return super.getViewTypeCount();
    }

    /** Translate a position from our coordinate space to the wrapped adapters coordinate space. */
    private int outerToInnerPosition(int position) {
        // No translation necessary for loading adapter, because the item appears at the end.
        return position;
    }

    @NonNull
    protected abstract View newLoadingView(@NonNull LayoutInflater layoutInflater,
                                           int position,
                                           @NonNull ViewGroup parent);

    private boolean isLoadingItem(int position) {
        // Loading item is the last item in the list.
        return position == getCount() - 1;
    }

    public static final class Builder {

        @NonNull
        private final ListAdapter mAdapter;

        @NonNull
        private final Data<?> mData;

        @Nullable
        private Item mLoadingItem;

        private boolean mOnlyShowIfEmpty;
        private boolean mLoadingItemEnabled;
        private boolean mTakeOwnership = true;

        public Builder(@NonNull ListAdapter adapter, @NonNull Data<?> data) {
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
        public Builder onlyShowIfEmpty(boolean onlyShowIfEmpty) {
            mOnlyShowIfEmpty = onlyShowIfEmpty;
            return this;
        }

        /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
        @NonNull
        public Builder takeOwnership(boolean takeOwnership) {
            mTakeOwnership = takeOwnership;
            return this;
        }

        @NonNull
        public LoadingAdapter build() {
            if (mLoadingItem == null) {
                throw new IllegalStateException("No loading item specified");
            }
            return new DataLoadingAdapter(mAdapter, mData, mLoadingItem, mLoadingItemEnabled,
                    mOnlyShowIfEmpty, mTakeOwnership);
        }
    }

    private static final class DataLoadingAdapter extends LoadingAdapter {

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

        private final boolean mLoadingItemEnabled;
        private final boolean mOnlyShowIfEmpty;

        DataLoadingAdapter(@NonNull ListAdapter adapter,
                           @NonNull Data<?> data,
                           @NonNull Item loadingItem,
                           boolean loadingItemEnabled,
                           boolean onlyShowIfEmpty,
                           boolean takeOwnership) {
            super(adapter, takeOwnership);
            mData = data;
            mLoadingItem = loadingItem;
            mLoadingItemEnabled = loadingItemEnabled;
            mOnlyShowIfEmpty = onlyShowIfEmpty;
            mData.registerDataObserver(mDataObserver);
            mData.registerLoadingObserver(mLoadingObserver);
        }

        @Override
        public void dispose() {
            mData.unregisterDataObserver(mDataObserver);
            mData.unregisterLoadingObserver(mLoadingObserver);
            super.dispose();
        }

        @Override
        protected boolean isLoading() {
            return mData.isLoading();
        }

        @NonNull
        @Override
        protected View newLoadingView(@NonNull LayoutInflater layoutInflater,
                                      int position,
                                      @NonNull ViewGroup parent) {
            return mLoadingItem.get(layoutInflater, parent);
        }

        @Override
        protected boolean isLoadingItemVisible() {
            boolean visible = super.isLoadingItemVisible();
            if (mOnlyShowIfEmpty) {
                return visible && isEmpty();
            }
            return visible;
        }

        @Override
        protected boolean isLoadingItemEnabled() {
            return mLoadingItemEnabled;
        }
    }
}