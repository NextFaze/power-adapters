package com.nextfaze.databind;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;

@Accessors(prefix = "m")
public final class PolymorphicMapper implements Mapper {

    @NonNull
    private final Map<Class<?>, Binder> mBinders = new HashMap<>();

    PolymorphicMapper(@NonNull Map<Class<?>, Binder> binders) {
        mBinders.putAll(binders);
    }

    @NonNull
    @Override
    public Binder getBinder(@NonNull Object item, int position) {
        Class<?> itemClass = item.getClass();
        Binder binder;
        while ((binder = mBinders.get(itemClass)) == null && itemClass != null) {
            itemClass = itemClass.getSuperclass();
        }
        if (binder == null) {
            throw new IllegalStateException("No binder found for item class hierarchy " + item.getClass());
        }
        return binder;
    }

    @NonNull
    @Override
    public Collection<Binder> getAllBinders() {
        return unmodifiableCollection(mBinders.values());
    }

    @NonNull
    private static Binder wrapBinder(@NonNull Binder binder, @LayoutRes final int overrideItemLayoutResource) {
        return new BinderWrapper(binder) {
            @NonNull
            @Override
            public View newView(@NonNull ViewGroup viewGroup) {
                return LayoutInflater.from(viewGroup.getContext())
                        .inflate(overrideItemLayoutResource, viewGroup, false);
            }
        };
    }

    public static final class Builder {

        @NonNull
        private final Map<Class<?>, Binder> mBinders = new HashMap<>();

        /** Map an item class to the specified binder, overriding the layout resource used to inflate the item view. */
        @NonNull
        public Builder bind(@NonNull Class<?> itemClass,
                            @LayoutRes final int overrideItemLayoutResource,
                            @NonNull Binder binder) {
            mBinders.put(itemClass, wrapBinder(binder, overrideItemLayoutResource));
            return this;
        }

        /** Map an item class to the specified binder. */
        @NonNull
        public Builder bind(@NonNull Class<?> itemClass, @NonNull Binder binder) {
            mBinders.put(itemClass, binder);
            return this;
        }

        @NonNull
        public PolymorphicMapper build() {
            if (mBinders.isEmpty()) {
                throw new IllegalStateException("Must have at least one binder defined");
            }
            return new PolymorphicMapper(mBinders);
        }
    }
}
