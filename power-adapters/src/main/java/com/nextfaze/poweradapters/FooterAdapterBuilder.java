package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.view.View;
import lombok.NonNull;

import java.util.ArrayList;

import static com.nextfaze.poweradapters.PowerAdapters.concat;

/** Wraps an existing {@link PowerAdapter} to provide footer views below the wrapped adapter's items. */
public final class FooterAdapterBuilder {

    @NonNull
    private final ArrayList<Item> mItems = new ArrayList<>();

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW;

    @NonNull
    public FooterAdapterBuilder addView(@NonNull View footerView) {
        return addView(footerView, false);
    }

    @NonNull
    public FooterAdapterBuilder addView(@NonNull View footerView, boolean enabled) {
        mItems.add(new Item(footerView, enabled));
        return this;
    }

    @NonNull
    public FooterAdapterBuilder addResource(@LayoutRes int footerResource) {
        return addResource(footerResource, false);
    }

    @NonNull
    public FooterAdapterBuilder addResource(@LayoutRes int footerResource, boolean enabled) {
        mItems.add(new Item(footerResource, enabled));
        return this;
    }

    @NonNull
    public FooterAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull final PowerAdapter adapter) {
        return concat(adapter, new HeaderFooterHelperAdapter(mItems, new HeaderFooterHelperAdapter.Policy() {
            @Override
            public boolean shouldShow() {
                return mEmptyPolicy.shouldShow(adapter);
            }
        }, adapter));
    }

    /** Evaluated to determine whether to show the footers. */
    public enum EmptyPolicy {
        /** Show the footers when the wrapped adapter is empty. */
        SHOW() {
            @Override
            boolean shouldShow(@NonNull PowerAdapter adapter) {
                return true;
            }
        },
        /** Hide the footers when the wrapped adapter is empty. */
        HIDE {
            @Override
            boolean shouldShow(@NonNull PowerAdapter adapter) {
                return adapter.getItemCount() > 0;
            }
        };

        /** Evaluate whether the items should show based on the wrapped adapter. */
        abstract boolean shouldShow(@NonNull PowerAdapter adapter);
    }
}
