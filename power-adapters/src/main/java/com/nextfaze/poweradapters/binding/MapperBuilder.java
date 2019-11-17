package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.nextfaze.poweradapters.binding.BinderWrapper.overrideLayout;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableSet;

/**
 * Fluent-style builder that may be used to construct a type-safe, complex {@link Mapper}. This mapper evaluates a list
 * of rules from first to last, and returns the {@link Binder} when both:
 * <ul>
 * <li>the item class is assignable to the rule class</li>
 * <li>the predicate, if present, evaluates to {@code true}</li>
 * </ul>
 */
public final class MapperBuilder<T> {

    private static final Predicate<Object> ALWAYS = new Predicate<Object>() {
        @Override
        public boolean apply(Object o) {
            return true;
        }
    };

    @NonNull
    private final List<Rule> mRules = new ArrayList<>();

    @Nullable
    private Boolean mStableIds;

    /**
     * Map an item type to the specified binder. The specified layout resource, if {@code >0}, will be used to override
     * the {@link View} normally inflated by the specified binder.
     * <p>
     * In addition, this method accepts a {@link Predicate} that will be evaluated per-instance to determine if the
     * specified binder is suitable for use with the item.
     * @param itemClass The type of item accepted by the specified binder.
     * @param overrideItemLayoutResource The layout resource that will be inflated instead of the view provided by
     * {@link Binder#newView(ViewGroup)}. May be {@code 0}, in which case this parameter does nothing.
     * @param binder The binder to be used to bind the specified item type.
     * @param predicate A predicate that will be evaluated for each item instance to determine if the specified binder
     * is suitable.
     * @return This builder, to allow chaining.
     */
    @NonNull
    public <E extends T> MapperBuilder<T> bind(@NonNull Class<E> itemClass,
                                               @LayoutRes int overrideItemLayoutResource,
                                               @NonNull Binder<? super E, ?> binder,
                                               @NonNull Predicate<? super E> predicate) {
        //noinspection unchecked
        mRules.add(new Rule(checkNotNull(itemClass, "itemClass"),
                (Predicate<Object>) checkNotNull(predicate, "predicate"),
                overrideLayout(checkNotNull(binder, "binder"), checkNotNull(overrideItemLayoutResource,
                        "overrideItemLayoutResource"))));
        return this;
    }

    /**
     * Map an item type to the specified binder. The specified layout resource, if {@code >0}, will be used to override
     * the {@link View} normally inflated by the specified binder.
     * @param itemClass The type of item accepted by the specified binder.
     * @param overrideItemLayoutResource The layout resource that will be inflated instead of the view provided by
     * {@link Binder#newView(ViewGroup)}. May be {@code 0}, in which case this parameter does nothing.
     * @param binder The binder to be used to bind the specified item type.
     * @return This builder, to allow chaining.
     */
    @NonNull
    public <E extends T> MapperBuilder<T> bind(@NonNull Class<E> itemClass,
                                               @LayoutRes int overrideItemLayoutResource,
                                               @NonNull Binder<? super E, ?> binder) {
        //noinspection unchecked
        return bind(itemClass, overrideItemLayoutResource, binder, ALWAYS);
    }

    /**
     * Map an item type to the specified binder.
     * <p>
     * In addition, this method accepts a {@link Predicate} that will be evaluated per-instance to determine if the
     * specified binder is suitable for use with the item.
     * @param itemClass The type of item accepted by the specified binder.
     * @param binder The binder to be used to bind the specified item type.
     * @param predicate A predicate that will be evaluated for each item instance to determine if the specified binder
     * is suitable.
     * @return This builder, to allow chaining.
     */
    @NonNull
    public <E extends T> MapperBuilder<T> bind(@NonNull Class<E> itemClass,
                                               @NonNull Binder<? super E, ?> binder,
                                               @NonNull Predicate<? super E> predicate) {
        return bind(itemClass, 0, binder, predicate);
    }

    /**
     * Map an item type to the specified binder.
     * @param itemClass The type of item accepted by the specified binder.
     * @param binder The binder to be used to bind the specified item type.
     * @return This builder, to allow chaining.
     */
    @NonNull
    public <E extends T> MapperBuilder<T> bind(@NonNull Class<E> itemClass,
                                               @NonNull Binder<? super E, ?> binder) {
        //noinspection unchecked
        return bind(itemClass, 0, binder, ALWAYS);
    }

    /**
     * Allows overriding whether or not the resulting {@link Mapper} will report as having stable IDs.
     * @param stableIds {@code true} forcefully enables stable IDs, {@code false} forcefully disables them, {@code
     * null} falls back to the default behaviour of {@link AbstractMapper#hasStableIds()}.
     * @return This builder, to allow chaining.
     * @see Mapper#hasStableIds()
     * @see AbstractMapper#hasStableIds()
     * @see PowerAdapter#hasStableIds()
     */
    @NonNull
    public MapperBuilder<T> stableIds(@Nullable Boolean stableIds) {
        mStableIds = stableIds;
        return this;
    }

    @NonNull
    public Mapper<T> build() {
        return new RuleMapper<>(new ArrayList<>(mRules), mStableIds);
    }

    private static final class RuleMapper<E> extends AbstractMapper<E> {

        @NonNull
        private final List<Rule> mRules;

        @NonNull
        private final Set<Binder<E, View>> mAllBinders;

        @Nullable
        private final Boolean mStableIds;

        RuleMapper(@NonNull List<Rule> rules, @Nullable Boolean stableIds) {
            mRules = rules;
            mStableIds = stableIds;
            Set<Binder<E, View>> allBinders = new HashSet<>();
            for (Rule rule : rules) {
                //noinspection unchecked
                allBinders.add((Binder<E, View>) rule.binder);
            }
            mAllBinders = unmodifiableSet(allBinders);
        }

        @Nullable
        @Override
        public Binder<E, View> getBinder(@NonNull E item, int position) {
            for (int i = 0; i < mRules.size(); i++) {
                Rule rule = mRules.get(i);
                if (rule.matches(item)) {
                    //noinspection unchecked
                    return (Binder<E, View>) rule.binder;
                }
            }
            return null;
        }

        @NonNull
        @Override
        public Collection<? extends Binder<E, View>> getAllBinders() {
            return mAllBinders;
        }

        @Override
        public boolean hasStableIds() {
            if (mStableIds != null) {
                return mStableIds;
            }
            return super.hasStableIds();
        }
    }

    private static final class Rule {

        @NonNull
        final Class<?> itemClass;

        @NonNull
        final Predicate<Object> predicate;

        @NonNull
        final Binder<?, ?> binder;

        Rule(@NonNull Class<?> itemClass,
             @NonNull Predicate<Object> predicate,
             @NonNull Binder<?, ?> binder) {
            this.itemClass = itemClass;
            this.predicate = predicate;
            this.binder = binder;
        }

        boolean matches(@NonNull Object item) {
            return itemClass.isAssignableFrom(item.getClass()) && predicate.apply(item);
        }
    }
}
