package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.DataObserver;
import com.nextfaze.asyncdata.LoadingObserver;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

import static com.nextfaze.poweradapters.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
public abstract class EmptyPowerAdapter extends PowerAdapterWrapper {

    protected EmptyPowerAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    /**
     * Determines if the empty item should currently be shown. By default, returns if the underlying adapter is empty.
     * Invoke {@link #notifyEmptyChanged()} to inform this adapter if the empty state has changed.
     * @return {@code true} if the empty item should be shown right now, otherwise {@code false}.
     */
    protected boolean isEmptyItemVisible() {
        return getAdapter().getItemCount() == 0;
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
    public final int getItemCount() {
        if (isEmptyItemVisible()) {
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
        if (isEmptyItemVisible() && isEmptyItem(position)) {
            return getEmptyViewType();
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        if (itemViewType == getEmptyViewType()) {
            return newEmptyView(layoutInflater(parent), parent);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, int position) {
        if (!isEmptyItem(position)) {
            super.bindView(view, position);
        }
    }

    @NonNull
    protected abstract View newEmptyView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent);

    private boolean isEmptyItem(int position) {
        return position == getItemCount() - 1;
    }

    private int getEmptyViewType() {
        return super.getViewTypeCount();
    }

    public static final class Builder {

        @NonNull
        private final Data<?> mData;

        @NonNull
        private final PowerAdapter mAdapter;

        @Nullable
        private Item mEmptyItem;

        private boolean mEmptyItemEnabled;
        private boolean mShowIfLoading;

        public Builder(@NonNull PowerAdapter adapter, @NonNull Data<?> data) {
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

        /** If {@code true}, empty item will be shown even while {@link Data} is loading. Defaults to {@code false}. */
        @NonNull
        public Builder showIfLoading(boolean showIfLoading) {
            mShowIfLoading = showIfLoading;
            return this;
        }

        @NonNull
        public EmptyPowerAdapter build() {
            if (mEmptyItem == null) {
                throw new IllegalStateException("No empty item specified");
            }
            return new DataEmptyAdapter(mAdapter, mData, mEmptyItem, mEmptyItemEnabled, mShowIfLoading);
        }
    }

    private static final class DataEmptyAdapter extends EmptyPowerAdapter {

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

        DataEmptyAdapter(@NonNull PowerAdapter adapter,
                         @NonNull Data<?> data,
                         @NonNull Item emptyItem,
                         boolean emptyItemEnabled,
                         boolean showIfLoading) {
            super(adapter);
            mData = data;
            mEmptyItem = emptyItem;
            mEmptyItemEnabled = emptyItemEnabled;
            mShowIfLoading = showIfLoading;
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
        protected View newEmptyView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
            return mEmptyItem.get(layoutInflater, parent);
        }
    }
}
