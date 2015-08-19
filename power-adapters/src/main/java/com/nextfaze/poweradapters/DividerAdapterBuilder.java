package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import lombok.NonNull;

public final class DividerAdapterBuilder {

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW_LEADING;

    @LayoutRes
    private int mLeadingResource;

    @LayoutRes
    private int mTrailingResource;

    @LayoutRes
    private int mInnerResource;

    /**
     * Set the policy that determines what dividers are shown if the wrapped adapter is empty. Defaults to {@link
     * EmptyPolicy#SHOW_LEADING}
     */
    @NonNull
    public DividerAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    /** Sets the layout resource of the divider that appears BEFORE the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder leadingResource(@LayoutRes int itemResource) {
        mLeadingResource = itemResource;
        return this;
    }

    /** Sets the layout resource of the divider that appears AFTER the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder trailingResource(@LayoutRes int itemResource) {
        mTrailingResource = itemResource;
        return this;
    }

    /** Sets the the layout resource of the divider that appears between all of the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder innerResource(@LayoutRes int itemResource) {
        mInnerResource = itemResource;
        return this;
    }

    /** Sets the layout resource of the divider that appears BEFORE and AFTER the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder outerResource(@LayoutRes int itemResource) {
        return leadingResource(itemResource).trailingResource(itemResource);
    }

    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        return new DividerAdapter(adapter, mEmptyPolicy, mLeadingResource, mTrailingResource,
                mInnerResource);
    }

    public enum EmptyPolicy {
        /** A single leading divider will be shown if the wrapped adapter is empty. */
        SHOW_LEADING,
        /** No dividers are shown if the wrapped adapter is empty. */
        SHOW_NOTHING
    }
}
