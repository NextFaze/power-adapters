package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;
import lombok.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForResource;

/** Inserts {@link View}-based dividers between items. */
public final class DividerAdapterBuilder {

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW_LEADING;

    @Nullable
    private Item mLeadingItem;

    @Nullable
    private Item mTrailingItem;

    @Nullable
    private Item mInnerItem;

    /**
     * Set the policy that determines what dividers are shown if the wrapped adapter is empty. Defaults to {@link
     * EmptyPolicy#SHOW_LEADING}.
     */
    @NonNull
    public DividerAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    /** Sets the divider that appears before the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder leadingView(@NonNull ViewFactory viewFactory) {
        mLeadingItem = new Item(viewFactory, false);
        return this;
    }

    /** Sets the divider that appears before the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder leadingResource(@LayoutRes int resource) {
        return leadingView(viewFactoryForResource(resource));
    }

    /** Sets the divider that appears after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder trailingView(@NonNull ViewFactory viewFactory) {
        mTrailingItem = new Item(viewFactory, false);
        return this;
    }

    /** Sets the divider that appears after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder trailingResource(@LayoutRes int resource) {
        return trailingView(viewFactoryForResource(resource));
    }

    /** Sets the divider that appears between all of the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder innerView(@NonNull ViewFactory viewFactory) {
        mInnerItem = new Item(viewFactory, false);
        return this;
    }

    /** Sets the divider that appears between all of the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder innerResource(@LayoutRes int resource) {
        return innerView(viewFactoryForResource(resource));
    }

    /** Sets the divider that appears before and after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder outerView(@NonNull ViewFactory viewFactory) {
        return leadingView(viewFactory).trailingView(viewFactory);
    }

    /** Sets the divider that appears before and after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder outerResource(@LayoutRes int resource) {
        return outerView(viewFactoryForResource(resource));
    }

    /** Sets the divider that appears before, after, and between the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder view(@NonNull ViewFactory viewFactory) {
        return outerView(viewFactory).innerView(viewFactory);
    }

    /** Sets the divider that appears before, after, and between the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder resource(@LayoutRes int resource) {
        return view(viewFactoryForResource(resource));
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        if (mLeadingItem == null && mTrailingItem == null && mInnerItem == null) {
            return adapter;
        }
        return new DividerAdapter(adapter, mEmptyPolicy, mLeadingItem, mTrailingItem, mInnerItem);
    }

    public enum EmptyPolicy {
        /** The leading divider will be shown if the wrapped adapter is empty. */
        SHOW_LEADING {
            @Override
            boolean shouldShowLeading(int itemCount) {
                return true;
            }
        },
        /** No dividers are shown if the wrapped adapter is empty. */
        SHOW_NOTHING;

        boolean shouldShowLeading(int itemCount) {
            return itemCount > 0;
        }

        boolean shouldShowTrailing(int itemCount) {
            return itemCount > 0;
        }

        boolean shouldShowInner(int itemCount) {
            return itemCount > 0;
        }
    }
}
