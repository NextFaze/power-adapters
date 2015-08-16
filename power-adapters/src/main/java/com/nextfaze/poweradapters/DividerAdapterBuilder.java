package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import lombok.NonNull;

public final class DividerAdapterBuilder {

    @NonNull
    private final PowerAdapter mAdapter;

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW_LEADING;

    @LayoutRes
    private int mLeadingItemResource;

    @LayoutRes
    private int mTrailingItemResource;

    @LayoutRes
    private int mInnerItemResource;

    public DividerAdapterBuilder(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Set the policy that determines what dividers are shown if the wrapped adapter is empty. Defaults to {@link
     * EmptyPolicy#SHOW_LEADING}
     */
    @NonNull
    public DividerAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    /** Sets layout resource of the divider that appears BEFORE the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder leadingItemResource(@LayoutRes int itemResource) {
        mLeadingItemResource = itemResource;
        return this;
    }

    /** Sets layout resource of the divider that appears AFTER the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder trailingItemResource(@LayoutRes int itemResource) {
        mTrailingItemResource = itemResource;
        return this;
    }

    /** Sets the layout resource of the divider that appears between all of the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder innerItemResource(@LayoutRes int itemResource) {
        mInnerItemResource = itemResource;
        return this;
    }

    /** Sets layout resource of the divider that appears BEFORE and AFTER the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder outerItemResource(@LayoutRes int itemResource) {
        return leadingItemResource(itemResource).trailingItemResource(itemResource);
    }

    @NonNull
    public DividerAdapter build() {
        return new DividerAdapter(mAdapter, mEmptyPolicy, mLeadingItemResource, mTrailingItemResource,
                mInnerItemResource);
    }

    public enum EmptyPolicy {
        /** A single leading divider will be shown if the wrapped adapter is empty. */
        SHOW_LEADING,
        /** No dividers are shown if the wrapped adapter is empty. */
        SHOW_NOTHING
    }
}
