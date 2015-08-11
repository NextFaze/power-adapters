package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.nextfaze.poweradapters.AdapterUtils.layoutInflater;

/**
 * Wraps an existing {@link PowerAdapter} to provide footer views below the wrapped adapters items. This class can be
 * subclassed for greater control over the presence of footer views.
 */
public abstract class FooterAdapter extends PowerAdapterWrapper {

    protected FooterAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    @NonNull
    abstract View getFooterView(@NonNull LayoutInflater layoutInflater,
                                          @NonNull ViewGroup parent,
                                          int position);

    abstract int getFooterCount(boolean visibleOnly);

    @Override
    public final int getItemCount() {
        return getFooterCount(true) + super.getItemCount();
    }

    @Override
    public final long getItemId(int position) {
        if (isFooterView(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
    }

    @Override
    public final int getViewTypeCount() {
        return super.getViewTypeCount() + getFooterCount(false);
    }

    @Override
    public final int getItemViewType(int position) {
        int itemViewType = footerItemViewType(position);
        if (itemViewType != -1) {
            return itemViewType;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        int footerIndex = itemViewTypeToFooterIndex(itemViewType);
        if (footerIndex != -1) {
            return getFooterView(layoutInflater(parent), parent, footerIndex);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        if (!isFooterView(holder.getPosition())) {
            super.bindView(view, holder);
        }
    }

    @Override
    protected int mapPosition(int outerPosition) {
        // No conversion necessary, as footers appear at the end.
        return outerPosition;
    }

    private boolean isFooterView(int position) {
        return position >= super.getItemCount();
    }

    private int footerItemViewType(int position) {
        if (!isFooterView(position)) {
            return -1;
        }
        return super.getViewTypeCount() + position - super.getItemCount();
    }

    private int itemViewTypeToFooterIndex(int itemViewType) {
        int superViewTypeCount = super.getViewTypeCount();
        if (itemViewType < superViewTypeCount) {
            return -1;
        }
        if (itemViewType >= getViewTypeCount()) {
            return -1;
        }
        return superViewTypeCount - itemViewType;
    }

    public enum VisibilityPolicy {
        ALWAYS() {
            @Override
            boolean shouldShow(@NonNull FooterAdapter adapter) {
                return true;
            }
        },
        HIDE_IF_EMPTY {
            @Override
            boolean shouldShow(@NonNull FooterAdapter adapter) {
                return adapter.getAdapter().getItemCount() > 0;
            }
        };

        abstract boolean shouldShow(@NonNull FooterAdapter adapter);
    }

    public static final class Builder {

        @NonNull
        private final PowerAdapter mAdapter;

        @NonNull
        private final ArrayList<Item> mFooters = new ArrayList<>();

        @NonNull
        private VisibilityPolicy mVisibilityPolicy = VisibilityPolicy.ALWAYS;

        public Builder(@NonNull PowerAdapter adapter) {
            mAdapter = adapter;
        }

        @NonNull
        public Builder footerView(@NonNull View footerView) {
            mFooters.add(new Item(footerView));
            return this;
        }

        @NonNull
        public Builder footerResource(@LayoutRes int footerResource) {
            mFooters.add(new Item(footerResource));
            return this;
        }

        @NonNull
        public Builder visibilityPolicy(@NonNull VisibilityPolicy visibilityPolicy) {
            mVisibilityPolicy = visibilityPolicy;
            return this;
        }

        @NonNull
        public FooterAdapter build() {
            return new Impl(mAdapter, mFooters, mVisibilityPolicy);
        }
    }

    private static final class Impl extends FooterAdapter {

        @NonNull
        private final ArrayList<Item> mFooters = new ArrayList<>();

        @NonNull
        private final VisibilityPolicy mVisibilityPolicy;

        Impl(@NonNull PowerAdapter adapter, @NonNull List<Item> footers, @NonNull VisibilityPolicy visibilityPolicy) {
            super(adapter);
            mVisibilityPolicy = visibilityPolicy;
            mFooters.addAll(footers);
        }

        @NonNull
        @Override
        protected View getFooterView(@NonNull LayoutInflater layoutInflater,
                                     @NonNull ViewGroup parent,
                                     int position) {
            return mFooters.get(position).get(layoutInflater, parent);
        }

        @Override
        protected int getFooterCount(boolean visibleOnly) {
            if (visibleOnly && !mVisibilityPolicy.shouldShow(this)) {
                return 0;
            }
            return mFooters.size();
        }
    }
}
