package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
final class EmptyAdapter extends PowerAdapterWrapper {

    // TODO: Consolidate with LoadingAdapter, because both add a single item at the end.

    @NonNull
    private final EmptyAdapterBuilder.Delegate mDelegate;

    @NonNull
    private final Item mEmptyItem;

    private final boolean mEmptyItemEnabled;

    private boolean mVisible;

    EmptyAdapter(@NonNull PowerAdapter adapter,
                 @NonNull EmptyAdapterBuilder.Delegate delegate,
                 @NonNull Item emptyItem,
                 boolean emptyItemEnabled) {
        super(adapter);
        mEmptyItem = emptyItem;
        mDelegate = delegate;
        mDelegate.setAdapter(this);
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

    void updateVisible() {
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

}
