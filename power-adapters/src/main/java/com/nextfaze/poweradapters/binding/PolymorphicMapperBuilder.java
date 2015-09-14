package com.nextfaze.poweradapters.binding;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;
import static java.util.Collections.unmodifiableCollection;

/** Mapper that binds an item object by traversing its class hierarchy until a binder is found. */
public final class PolymorphicMapperBuilder {

    @NonNull
    private final Map<Class<?>, Binder> mBinders = new HashMap<>();

    private boolean mStableIds;

    /** Map an item class to the specified binder, overriding the layout resource used to inflate the item view. */
    @NonNull
    public PolymorphicMapperBuilder bind(@NonNull Class<?> itemClass,
                                         @LayoutRes int overrideItemLayoutResource,
                                         @NonNull Binder binder) {
        mBinders.put(itemClass, wrapBinder(binder, overrideItemLayoutResource));
        return this;
    }

    /** Map an item class to the specified binder. */
    @NonNull
    public PolymorphicMapperBuilder bind(@NonNull Class<?> itemClass, @NonNull Binder binder) {
        mBinders.put(itemClass, binder);
        return this;
    }

    @NonNull
    public PolymorphicMapperBuilder stableIds(boolean stableIds) {
        mStableIds = stableIds;
        return this;
    }

    @CheckResult
    @NonNull
    public Mapper build() {
        return new PolymorphicMapper(mBinders, mStableIds);
    }

    @NonNull
    private static Binder wrapBinder(@NonNull Binder binder, @LayoutRes final int overrideItemLayoutResource) {
        return new BinderWrapper(binder) {
            @NonNull
            @Override
            public View newView(@NonNull ViewGroup viewGroup) {
                return layoutInflater(viewGroup).inflate(overrideItemLayoutResource, viewGroup, false);
            }
        };
    }

    @Accessors(prefix = "m")
    private static final class PolymorphicMapper implements Mapper {

        @NonNull
        private final Map<Class<?>, Binder> mBinders = new HashMap<>();

        private final boolean mStableIds;

        PolymorphicMapper(@NonNull Map<Class<?>, Binder> binders, boolean stableIds) {
            mBinders.putAll(binders);
            mStableIds = stableIds;
        }

        @Nullable
        @Override
        public Binder getBinder(@NonNull Object item, int position) {
            Class<?> itemClass = item.getClass();
            // Traverse item class hierarchy looking for a binder.
            Binder binder;
            while ((binder = mBinders.get(itemClass)) == null && itemClass != null) {
                itemClass = itemClass.getSuperclass();
            }
            // Return null if no binder found.
            return binder;
        }

        @NonNull
        @Override
        public Collection<? extends Binder> getAllBinders() {
            return unmodifiableCollection(mBinders.values());
        }

        @Override
        public boolean hasStableIds() {
            return mStableIds;
        }
    }
}
