package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;
import lombok.NonNull;

import java.util.ArrayList;

import static com.nextfaze.poweradapters.Conditions.adapter;
import static com.nextfaze.poweradapters.Conditions.and;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForResource;
import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForView;

/** Wraps an existing {@link PowerAdapter} to provide header views above the wrapped adapter's items. */
@Deprecated
public final class HeaderAdapterBuilder implements Decorator {

    @NonNull
    private final ArrayList<Item> mItems = new ArrayList<>();

    @NonNull
    private EmptyPolicy mEmptyPolicy = EmptyPolicy.SHOW;

    @Nullable
    private Condition mCondition;

    /**
     * Not safe for use in a {@code RecyclerView}.
     * @see ViewFactories#viewFactoryForView(View)
     */
    @Deprecated
    @NonNull
    public HeaderAdapterBuilder addView(@NonNull View view) {
        return addView(view, false);
    }

    /**
     * Not safe for use in a {@code RecyclerView}.
     * @see ViewFactories#viewFactoryForView(View)
     */
    @Deprecated
    @NonNull
    public HeaderAdapterBuilder addView(@NonNull View view, boolean enabled) {
        return addView(viewFactoryForView(view), enabled);
    }

    @NonNull
    public HeaderAdapterBuilder addResource(@LayoutRes int resource) {
        return addResource(resource, false);
    }

    @NonNull
    public HeaderAdapterBuilder addResource(@LayoutRes int resource, boolean enabled) {
        return addView(viewFactoryForResource(resource), enabled);
    }

    @NonNull
    public HeaderAdapterBuilder addView(@NonNull ViewFactory viewFactory) {
        return addView(viewFactory, false);
    }

    @NonNull
    public HeaderAdapterBuilder addView(@NonNull ViewFactory viewFactory, boolean enabled) {
        mItems.add(new Item(viewFactory, enabled));
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder emptyPolicy(@NonNull EmptyPolicy emptyPolicy) {
        mEmptyPolicy = emptyPolicy;
        return this;
    }

    @NonNull
    public HeaderAdapterBuilder condition(@Nullable Condition condition) {
        mCondition = condition;
        return this;
    }

    @CheckResult
    @NonNull
    public PowerAdapter build(@NonNull PowerAdapter adapter) {
        if (mItems.isEmpty()) {
            return adapter;
        }
        Condition condition = mEmptyPolicy.asCondition(adapter);
        if (mCondition != null) {
            condition = and(condition, mCondition);
        }
        return adapter.prepend(asAdapter(mItems).showOnlyWhile(condition));
    }

    @NonNull
    @Override
    public PowerAdapter decorate(@NonNull PowerAdapter adapter) {
        return build(adapter);
    }

    /** Evaluated to determine whether to show the headers. */
    @Deprecated
    public enum EmptyPolicy {
        /** Show the headers when the wrapped adapter is empty. */
        SHOW {
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

        @NonNull
        Condition asCondition(@NonNull PowerAdapter adapter) {
            return adapter(adapter, new Predicate<PowerAdapter>() {
                @Override
                public boolean apply(PowerAdapter adapter) {
                    return shouldShow(adapter);
                }
            });
        }
    }
}
