package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.LoadingObserver;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
public abstract class EmptyAdapter extends PowerAdapterWrapper {

    // TODO: Consolidate with LoadingAdapter, because both add a single item at the end.

    private boolean mVisible;

    protected EmptyAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    /**
     * Determines if the empty item should currently be shown. By default, returns if the underlying adapter is empty.
     * Invoke {@link #notifyEmptyChanged()} to inform this adapter if the empty state has changed.
     * @return {@code true} if the empty item should be shown right now, otherwise {@code false}.
     */
    protected boolean isEmptyItemVisible() {
        return super.getItemCount() == 0;
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
        updateVisible();
    }

    private void updateVisible() {
        boolean visible = isEmptyItemVisible();
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
            return isEmptyItemEnabled();
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
    protected abstract View newEmptyView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent);

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

    public enum LoadingPolicy {
        /** Empty item will be shown even while {@link Data} is loading. */
        SHOW,
        /** Empty item will not be shown while {@link Data} is loading. */
        HIDE
    }

    public static final class Builder {

        @NonNull
        private final Data<?> mData;

        @NonNull
        private final PowerAdapter mAdapter;

        @Nullable
        private Item mEmptyItem;

        @NonNull
        private LoadingPolicy mLoadingPolicy = LoadingPolicy.HIDE;

        private boolean mEmptyItemEnabled;

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

        /** Set the policy to use when {@link Data} is loading. Use {@link LoadingPolicy#HIDE} by default. */
        @NonNull
        public Builder loadingPolicy(@NonNull LoadingPolicy loadingPolicy) {
            mLoadingPolicy = loadingPolicy;
            return this;
        }

        @NonNull
        public EmptyAdapter build() {
            if (mEmptyItem == null) {
                throw new IllegalStateException("No empty item specified");
            }
            return new Impl(mAdapter, mData, mEmptyItem, mLoadingPolicy, mEmptyItemEnabled);
        }
    }

    private static final class Impl extends EmptyAdapter {

        @NonNull
        private final Data<?> mData;

        @NonNull
        private final com.nextfaze.asyncdata.DataObserver mDataObserver = new com.nextfaze.asyncdata.SimpleDataObserver() {
            @Override
            public void onChange() {
                notifyEmptyChanged();
            }
        };

        @NonNull
        private final LoadingObserver mLoadingObserver = new LoadingObserver() {
            @Override
            public void onLoadingChange() {
                notifyEmptyChanged();
            }
        };

        @NonNull
        private final Item mEmptyItem;

        @NonNull
        private final LoadingPolicy mLoadingPolicy;

        private final boolean mEmptyItemEnabled;

        Impl(@NonNull PowerAdapter adapter,
             @NonNull Data<?> data,
             @NonNull Item emptyItem,
             @NonNull LoadingPolicy loadingPolicy,
             boolean emptyItemEnabled) {
            super(adapter);
            mData = data;
            mEmptyItem = emptyItem;
            mLoadingPolicy = loadingPolicy;
            mEmptyItemEnabled = emptyItemEnabled;
            // Notify immediately to ensure current Data state is accounted for.
            notifyEmptyChanged();
        }

        @Override
        protected void onFirstObserverRegistered() {
            super.onFirstObserverRegistered();
            mData.registerDataObserver(mDataObserver);
            mData.registerLoadingObserver(mLoadingObserver);
        }

        @Override
        protected void onLastObserverUnregistered() {
            super.onLastObserverUnregistered();
            mData.unregisterDataObserver(mDataObserver);
            mData.unregisterLoadingObserver(mLoadingObserver);
        }

        @Override
        protected boolean isEmptyItemVisible() {
            if (mLoadingPolicy == LoadingPolicy.SHOW) {
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
