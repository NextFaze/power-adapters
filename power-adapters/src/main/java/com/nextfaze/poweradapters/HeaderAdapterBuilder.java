package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/** Wraps an existing {@link PowerAdapter} to provide header views above the wrapped adapter's items. */
public final class HeaderAdapterBuilder {

    @NonNull
    private final ArrayList<Item> mHeaders = new ArrayList<>();

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW;

    @NonNull
    public HeaderAdapterBuilder headerView(@NonNull View headerView) {
        return headerView(headerView, false);
    }

    @NonNull
    public HeaderAdapterBuilder headerView(@NonNull View headerView, boolean enabled) {
        mHeaders.add(new Item(headerView, enabled));
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder headerResource(@LayoutRes int headerResource) {
        return headerResource(headerResource, false);
    }

    @NonNull
    public HeaderAdapterBuilder headerResource(@LayoutRes int headerResource, boolean enabled) {
        mHeaders.add(new Item(headerResource, enabled));
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        return new Impl(adapter, mHeaders, mEmptyPolicy);
    }

    public enum EmptyPolicy {
        /** Show the headers when the wrapped adapter is empty. */
        SHOW() {
            @Override
            boolean shouldShow(@NonNull HeaderAdapter adapter) {
                return true;
            }
        },
        /** Hide the headers when the wrapped adapter is empty. */
        HIDE {
            @Override
            boolean shouldShow(@NonNull HeaderAdapter adapter) {
                return adapter.getAdapter().getItemCount() > 0;
            }
        };

        abstract boolean shouldShow(@NonNull HeaderAdapter adapter);
    }

    private static final class Impl extends HeaderAdapter {

        @NonNull
        private final ArrayList<Item> mItems;

        @NonNull
        private final EmptyPolicy mEmptyPolicy;

        Impl(@NonNull PowerAdapter adapter, @NonNull List<Item> footers, @NonNull EmptyPolicy emptyPolicy) {
            super(adapter);
            mItems = new ArrayList<>(footers);
            mEmptyPolicy = emptyPolicy;
        }

        @NonNull
        @Override
        View getHeaderView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent, int index) {
            return mItems.get(index).get(layoutInflater, parent);
        }

        @Override
        boolean isHeaderEnabled(int index) {
            return mItems.get(index).isEnabled();
        }

        @Override
        int getHeaderCount(boolean visibleOnly) {
            if (visibleOnly && !mEmptyPolicy.shouldShow(this)) {
                return 0;
            }
            return mItems.size();
        }
    }
}
