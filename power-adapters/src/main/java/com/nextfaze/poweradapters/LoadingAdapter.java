package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

/** Wraps an existing {@link PowerAdapter} and displays a loading indicator while loading. */
@Accessors(prefix = "m")
final class LoadingAdapter extends PowerAdapterWrapper {

    @NonNull
    private LoadingAdapterBuilder.Delegate mDelegate;

    @NonNull
    private Item mLoadingItem;

    @NonNull
    private LoadingAdapterBuilder.EmptyPolicy mEmptyPolicy;

    private boolean mVisible;

    private boolean mLoadingItemEnabled;

    LoadingAdapter(@NonNull PowerAdapter adapter,
                   @NonNull Item loadingItem,
                   @NonNull LoadingAdapterBuilder.EmptyPolicy emptyPolicy,
                   @NonNull LoadingAdapterBuilder.Delegate delegate,
                   boolean loadingItemEnabled) {
        super(adapter);
        mLoadingItem = loadingItem;
        mEmptyPolicy = emptyPolicy;
        mDelegate = delegate;
        mDelegate.setAdapter(this);
        mLoadingItemEnabled = loadingItemEnabled;
        updateVisible();
    }

    @Override
    public int getItemCount() {
        if (mVisible) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    @Override
    public int getViewTypeCount() {
        // Fixed amount of view types: whatever the underlying adapter wants, plus our loading item.
        return super.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingItem(position)) {
            return loadingViewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        if (isLoadingItem(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
    }

    @Override
    public boolean isEnabled(int position) {
        if (isLoadingItem(position)) {
            return mLoadingItemEnabled;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        if (itemViewType == loadingViewType()) {
            return newLoadingView(layoutInflater(parent), parent);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
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

    void updateVisible() {
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

}