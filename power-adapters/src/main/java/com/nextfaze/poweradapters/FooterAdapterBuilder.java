package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/** Wraps an existing {@link PowerAdapter} to provide footer views below the wrapped adapter's items. */
public final class FooterAdapterBuilder {

    @NonNull
    private final ArrayList<Item> mFooters = new ArrayList<>();

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW;

    @NonNull
    public FooterAdapterBuilder footerView(@NonNull View footerView) {
        return footerView(footerView, false);
    }

    @NonNull
    public FooterAdapterBuilder footerView(@NonNull View footerView, boolean enabled) {
        mFooters.add(new Item(footerView, enabled));
        return this;
    }

    @NonNull
    public FooterAdapterBuilder footerResource(@LayoutRes int footerResource) {
        return footerResource(footerResource, false);
    }

    @NonNull
    public FooterAdapterBuilder footerResource(@LayoutRes int footerResource, boolean enabled) {
        mFooters.add(new Item(footerResource, enabled));
        return this;
    }

    @NonNull
    public FooterAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        return new Impl(adapter, mFooters, mEmptyPolicy);
    }

    public enum EmptyPolicy {
        /** Show the footers when the wrapped adapter is empty. */
        SHOW() {
            @Override
            boolean shouldShow(@NonNull FooterAdapter adapter) {
                return true;
            }
        },
        /** Hide the footers when the wrapped adapter is empty. */
        HIDE {
            @Override
            boolean shouldShow(@NonNull FooterAdapter adapter) {
                return adapter.getAdapter().getItemCount() > 0;
            }
        };

        abstract boolean shouldShow(@NonNull FooterAdapter adapter);
    }

    private static final class Impl extends FooterAdapter {

        @NonNull
        private final ArrayList<Item> mItems;

        @NonNull
        private final EmptyPolicy mEmptyPolicy;

        Impl(@NonNull PowerAdapter adapter, @NonNull List<Item> items, @NonNull EmptyPolicy emptyPolicy) {
            super(adapter);
            mItems = new ArrayList<>(items);
            mEmptyPolicy = emptyPolicy;
        }

        @NonNull
        @Override
        View getFooterView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent, int index) {
            return mItems.get(index).get(layoutInflater, parent);
        }

        @Override
        boolean isFooterEnabled(int index) {
            return mItems.get(index).isEnabled();
        }

        @Override
        int getFooterCount(boolean visibleOnly) {
            if (visibleOnly && !mEmptyPolicy.shouldShow(this)) {
                return 0;
            }
            return mItems.size();
        }
    }
}
