package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.view.View;
import com.nextfaze.poweradapters.HeaderFooterHelperAdapter.Policy;
import lombok.NonNull;

import java.util.ArrayList;

import static com.nextfaze.poweradapters.PowerAdapters.concat;

/** Wraps an existing {@link PowerAdapter} to provide header views above the wrapped adapter's items. */
public final class HeaderAdapterBuilder {

    @NonNull
    private final ArrayList<Item> mItems = new ArrayList<>();

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW;

    @NonNull
    public HeaderAdapterBuilder addView(@NonNull View headerView) {
        return addView(headerView, false);
    }

    @NonNull
    public HeaderAdapterBuilder addView(@NonNull View headerView, boolean enabled) {
        mItems.add(new Item(headerView, enabled));
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder addResource(@LayoutRes int headerResource) {
        return addResource(headerResource, false);
    }

    @NonNull
    public HeaderAdapterBuilder addResource(@LayoutRes int headerResource, boolean enabled) {
        mItems.add(new Item(headerResource, enabled));
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull final PowerAdapter adapter) {
        return concat(new HeaderFooterHelperAdapter(mItems, new Policy() {
            @Override
            public boolean shouldShow() {
                return mEmptyPolicy.shouldShow(adapter);
            }
        }, adapter), adapter);
    }

    /** Evaluated to determine whether to show the headers. */
    public enum EmptyPolicy {
        /** Show the headers when the wrapped adapter is empty. */
        SHOW() {
            @Override
            boolean shouldShow(@NonNull PowerAdapter adapter) {
                return true;
            }
        },
        /** Hide the headers when the wrapped adapter is empty. */
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
