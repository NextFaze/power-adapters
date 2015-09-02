package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

@Accessors(prefix = "m")
final class DividerAdapter extends PowerAdapterWrapper {

    @NonNull
    private final ViewType mLeadingViewType = new ViewType();

    @NonNull
    private final ViewType mTrailingViewType = new ViewType();

    @NonNull
    private final ViewType mInnerViewType = new ViewType();

    @NonNull
    private final DividerAdapterBuilder.EmptyPolicy mEmptyPolicy;

    @LayoutRes
    private final int mLeadingItemResource;

    @LayoutRes
    private final int mTrailingItemResource;

    @LayoutRes
    private final int mInnerItemResource;

    DividerAdapter(@NonNull PowerAdapter adapter,
                   @NonNull DividerAdapterBuilder.EmptyPolicy emptyPolicy,
                   @LayoutRes int leadingItemResource,
                   @LayoutRes int trailingItemResource,
                   @LayoutRes int innerItemResource) {
        super(adapter);
        mLeadingItemResource = leadingItemResource;
        mTrailingItemResource = trailingItemResource;
        mInnerItemResource = innerItemResource;
        mEmptyPolicy = emptyPolicy;
    }

    @Override
    public int getItemCount() {
        int superCount = super.getItemCount();
        if (mEmptyPolicy != DividerAdapterBuilder.EmptyPolicy.SHOW_LEADING && superCount <= 0) {
            return 0;
        }
        int count;
        if (mInnerItemResource > 0) {
            count = superCount * 2 - 1;
        } else {
            count = superCount;
        }
        if (mLeadingItemResource > 0) {
            ++count;
        }
        if (mTrailingItemResource > 0) {
            ++count;
        }
        return count;
    }

    @Override
    public long getItemId(int position) {
        if (isDivider(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        if (isInnerDivider(position)) {
            return mInnerViewType;
        }
        if (isLeadingDivider(position)) {
            return mLeadingViewType;
        }
        if (isTrailingDivider(position)) {
            return mTrailingViewType;
        }
        return super.getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isDivider(position)) {
            return false;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        if (viewType == mInnerViewType) {
            return newInnerDividerView(layoutInflater(parent), parent);
        }
        if (viewType == mLeadingViewType) {
            return newLeadingDividerView(layoutInflater(parent), parent);
        }
        if (viewType == mTrailingViewType) {
            return newTrailingDividerView(layoutInflater(parent), parent);
        }
        return super.newView(parent, viewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        ViewType viewType = getItemViewType(holder.getPosition());
        if (viewType == mInnerViewType) {
            return;
        }
        if (viewType == mLeadingViewType) {
            return;
        }
        if (viewType == mTrailingViewType) {
            return;
        }
        super.bindView(view, holder);
    }

    @Override
    protected int outerToInner(int outerPosition) {
        if (mLeadingItemResource > 0) {
            return (outerPosition - 1) / 2;
        }
        return outerPosition / 2;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        if (mLeadingItemResource > 0) {
            return (innerPosition * 2) + 1;
        }
        return innerPosition * 2;
    }

    @NonNull
    private View newLeadingDividerView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(mLeadingItemResource, parent, false);
    }

    @NonNull
    private View newTrailingDividerView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(mTrailingItemResource, parent, false);
    }

    @NonNull
    private View newInnerDividerView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(mInnerItemResource, parent, false);
    }

    private boolean isDivider(int position) {
        return isInnerDivider(position) || isLeadingDivider(position) || isTrailingDivider(position);
    }

    private boolean isInnerDivider(int position) {
        if (isLeadingDivider(position)) {
            return false;
        }
        if (isTrailingDivider(position)) {
            return false;
        }
        if (mLeadingItemResource <= 0) {
            ++position;
        }
        //noinspection SimplifiableIfStatement
        if (mInnerItemResource > 0) {
            return position % 2 == 0;
        }
        return false;
    }

    private boolean isLeadingDivider(int position) {
        return mLeadingItemResource > 0 && position == 0;
    }

    private boolean isTrailingDivider(int position) {
        return mTrailingItemResource > 0 && position == getItemCount() - 1;
    }
}
