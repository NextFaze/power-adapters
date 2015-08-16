package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.View;
import lombok.NonNull;

import java.util.ArrayList;

/** Wraps an existing {@link PowerAdapter} to provide footer views below the wrapped adapter's items. */
public final class FooterAdapterBuilder {

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private final ArrayList<Item> mFooters = new ArrayList<>();

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW;

    public FooterAdapterBuilder(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
    }

    @NonNull
    public FooterAdapterBuilder footerView(@NonNull View footerView) {
        mFooters.add(new Item(footerView));
        return this;
    }

    @NonNull
    public FooterAdapterBuilder footerResource(@LayoutRes int footerResource) {
        mFooters.add(new Item(footerResource));
        return this;
    }

    @NonNull
    public FooterAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    @NonNull
    public FooterAdapter build() {
        return new FooterAdapter.Impl(mAdapter, mFooters, mEmptyPolicy);
    }

    public enum EmptyPolicy {
        SHOW() {
            @Override
            boolean shouldShow(@NonNull FooterAdapter adapter) {
                return true;
            }
        },
        HIDE {
            @Override
            boolean shouldShow(@NonNull FooterAdapter adapter) {
                return adapter.getAdapter().getItemCount() > 0;
            }
        };

        abstract boolean shouldShow(@NonNull FooterAdapter adapter);
    }
}
