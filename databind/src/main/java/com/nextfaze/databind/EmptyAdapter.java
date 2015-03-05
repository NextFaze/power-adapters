package com.nextfaze.databind;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

import static com.nextfaze.databind.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
public abstract class EmptyAdapter extends ListAdapterWrapper {

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter) */
    protected EmptyAdapter(@NonNull ListAdapter adapter) {
        super(adapter);
    }

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
    protected EmptyAdapter(@NonNull ListAdapter adapter, boolean takeOwnership) {
        super(adapter, takeOwnership);
    }

    /**
     * Determines if the empty item should currently be shown. By default, returns if the underlying adapter is empty.
     * Invoke {@link #notifyEmptyChanged()} to inform this adapter if the empty state has changed.
     * @return {@code true} if the empty item should be shown right now, otherwise {@code false}.
     */
    protected boolean isEmptyItemVisible() {
        return mAdapter.isEmpty();
    }

    /**
     * Determines whether the empty item should be enabled in the list, allowing it to be clicked or not.
     * <p/>
     * Returns {@code false} by default.
     * @return {@code true} if the empty item should be enabled, otherwise {@code false}.
     * @see ListAdapter#isEnabled(int)
     */
    protected boolean isEmptyItemEnabled() {
        return false;
    }

    protected final void notifyEmptyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public final int getCount() {
        if (isEmptyItemVisible()) {
            return mAdapter.getCount() + 1;
        }
        return mAdapter.getCount();
    }

    @Override
    public final int getViewTypeCount() {
        // Fixed amount of view types: whatever the underlying adapter wants, plus our empty item.
        return super.getViewTypeCount() + 1;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isEmptyItemVisible() && isEmptyItem(position)) {
            return getEmptyViewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == getEmptyViewType()) {
            if (convertView == null) {
                convertView = newEmptyView(layoutInflater(parent), position, parent);
            }
            return convertView;
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public final boolean isEnabled(int position) {
        if (isEmptyItemVisible() && isEmptyItem(position)) {
            return isEmptyItemEnabled();
        }
        return super.isEnabled(position);
    }

    @NonNull
    protected abstract View newEmptyView(@NonNull LayoutInflater layoutInflater,
                                         int position,
                                         @NonNull ViewGroup parent);

    private boolean isEmptyItem(int position) {
        return position == getCount() - 1;
    }

    private int getEmptyViewType() {
        return super.getViewTypeCount();
    }

    public static final class Builder {

        @NonNull
        private final Data<?> mData;

        @NonNull
        private final ListAdapter mAdapter;

        @Nullable
        private Item mEmptyItem;

        private boolean mEmptyItemEnabled;
        private boolean mShowIfLoading;
        private boolean mTakeOwnership = true;

        public Builder(@NonNull Data<?> data, @NonNull ListAdapter adapter) {
            mData = data;
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
        public Builder showIfLoading(boolean showIfLoading) {
            mShowIfLoading = showIfLoading;
            return this;
        }

        /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
        @NonNull
        public Builder takeOwnership(boolean takeOwnership) {
            mTakeOwnership = takeOwnership;
            return this;
        }

        @NonNull
        public EmptyAdapter build() {
            if (mEmptyItem == null) {
                throw new IllegalStateException("No empty item specified");
            }
            return new DataEmptyAdapter(mAdapter, mData, mEmptyItem, mEmptyItemEnabled, mShowIfLoading, mTakeOwnership);
        }
    }

    private static final class DataEmptyAdapter extends EmptyAdapter {

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
                notifyDataSetChanged();
            }
        };

        @NonNull
        private final Item mEmptyItem;

        private final boolean mEmptyItemEnabled;
        private final boolean mShowIfLoading;

        DataEmptyAdapter(@NonNull ListAdapter adapter,
                         @NonNull Data<?> data,
                         @NonNull Item emptyItem,
                         boolean emptyItemEnabled,
                         boolean showIfLoading,
                         boolean takeOwnership) {
            super(adapter, takeOwnership);
            mData = data;
            mEmptyItem = emptyItem;
            mEmptyItemEnabled = emptyItemEnabled;
            mShowIfLoading = showIfLoading;
            mData.registerDataObserver(mDataObserver);
            mData.registerLoadingObserver(mLoadingObserver);
        }

        @Override
        public void dispose() {
            super.dispose();
            mData.unregisterDataObserver(mDataObserver);
            mData.unregisterLoadingObserver(mLoadingObserver);
        }

        @Override
        protected boolean isEmptyItemVisible() {
            if (mShowIfLoading) {
                return mData.isEmpty();
            }
            return !mData.isLoading() && mData.isEmpty();
        }

        @Override
        protected boolean isEmptyItemEnabled() {
            return mEmptyItemEnabled;
        }

        @NonNull
        @Override
        protected View newEmptyView(@NonNull LayoutInflater layoutInflater, int position, @NonNull ViewGroup parent) {
            return mEmptyItem.get(layoutInflater, parent);
        }
    }
}
