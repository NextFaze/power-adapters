package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.View;
import lombok.NonNull;

import java.util.ArrayList;

/** Wraps an existing {@link PowerAdapter} to provide header views above the wrapped adapter's items. */
public final class HeaderAdapterBuilder {

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final ArrayList<Item> mHeaders = new ArrayList<>();

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW;

    public HeaderAdapterBuilder(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
    }

    @NonNull
    public HeaderAdapterBuilder headerView(@NonNull View headerView) {
        mHeaders.add(new Item(headerView));
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder headerResource(@LayoutRes int headerResource) {
        mHeaders.add(new Item(headerResource));
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    @NonNull
    public HeaderAdapter build() {
        return new HeaderAdapter.Impl(mAdapter, mHeaders, mEmptyPolicy);
    }

    public enum EmptyPolicy {
        SHOW() {
            @Override
            boolean shouldShow(@NonNull HeaderAdapter adapter) {
                return true;
            }
        },
        HIDE {
            @Override
            boolean shouldShow(@NonNull HeaderAdapter adapter) {
                return adapter.getAdapter().getItemCount() > 0;
            }
        };

        abstract boolean shouldShow(@NonNull HeaderAdapter adapter);
    }
}
