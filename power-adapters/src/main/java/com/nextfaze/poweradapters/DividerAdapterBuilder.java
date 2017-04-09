package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/** Inserts {@link View}-based dividers between items. */
public final class DividerAdapterBuilder implements PowerAdapter.Transformer {

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.DEFAULT;

    @Nullable
    private Item mLeadingItem;

    @Nullable
    private Item mTrailingItem;

    @Nullable
    private Item mInnerItem;

    /**
     * Set the policy that determines what dividers are shown if the wrapped adapter is empty. Defaults to {@link
     * EmptyPolicy#DEFAULT}.
     */
    @NonNull
    public DividerAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = checkNotNull(emptyPolicy, "emptyPolicy");
        return this;
    }

    /** Sets the divider that appears before the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder leadingView(@NonNull ViewFactory viewFactory) {
        mLeadingItem = new Item(checkNotNull(viewFactory, "viewFactory"), false);
        return this;
    }

    /** Sets the divider that appears before the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder leadingResource(@LayoutRes int resource) {
        return leadingView(asViewFactory(resource));
    }

    /** Sets the divider that appears after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder trailingView(@NonNull ViewFactory viewFactory) {
        mTrailingItem = new Item(checkNotNull(viewFactory, "viewFactory"), false);
        return this;
    }

    /** Sets the divider that appears after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder trailingResource(@LayoutRes int resource) {
        return trailingView(asViewFactory(resource));
    }

    /** Sets the divider that appears between all of the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder innerView(@NonNull ViewFactory viewFactory) {
        mInnerItem = new Item(checkNotNull(viewFactory, "viewFactory"), false);
        return this;
    }

    /** Sets the divider that appears between all of the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder innerResource(@LayoutRes int resource) {
        return innerView(asViewFactory(resource));
    }

    /** Sets the divider that appears before and after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder outerView(@NonNull ViewFactory viewFactory) {
        return leadingView(viewFactory).trailingView(viewFactory);
    }

    /** Sets the divider that appears before and after the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder outerResource(@LayoutRes int resource) {
        return outerView(asViewFactory(resource));
    }

    /** Sets the divider that appears before, after, and between the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder view(@NonNull ViewFactory viewFactory) {
        checkNotNull(viewFactory, "viewFactory");
        return outerView(viewFactory).innerView(viewFactory);
    }

    /** Sets the divider that appears before, after, and between the wrapped adapters items. */
    @NonNull
    public DividerAdapterBuilder resource(@LayoutRes int resource) {
        return view(asViewFactory(resource));
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        checkNotNull(adapter, "adapter");
        if (mLeadingItem == null && mTrailingItem == null && mInnerItem == null) {
            return adapter;
        }
        return new WrappingDividerAdapter(adapter, mEmptyPolicy, mLeadingItem, mTrailingItem, mInnerItem);
    }

    @NonNull
    @Override
    public PowerAdapter transform(@NonNull PowerAdapter adapter) {
        return build(adapter);
    }

    public enum EmptyPolicy {
        /** The leading divider will be shown if the wrapped adapter is empty. */
        SHOW_LEADING {
            @Override
            boolean shouldShowLeading(int itemCount) {
                return true;
            }
        },
        /** The trailing divider will be shown if the wrapped adapter is empty. */
        SHOW_TRAILING {
            @Override
            boolean shouldShowTrailing(int itemCount) {
                return true;
            }
        },
        /** The leading and trailing dividers will be shown if the wrapped adapter is empty. */
        SHOW_LEADING_AND_TRAILING {
            @Override
            boolean shouldShowLeading(int itemCount) {
                return true;
            }

            @Override
            boolean shouldShowTrailing(int itemCount) {
                return true;
            }
        },
        /** No dividers are shown if the wrapped adapter is empty. */
        SHOW_NOTHING;

        public static final EmptyPolicy DEFAULT = SHOW_LEADING;

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
