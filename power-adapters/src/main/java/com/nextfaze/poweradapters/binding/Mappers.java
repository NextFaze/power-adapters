package com.nextfaze.poweradapters.binding;

import android.view.View;

import java.util.Collection;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static java.util.Collections.singleton;

public final class Mappers {

    private Mappers() {
    }

    /** Creates a {@link Mapper} that only ever uses the single supplied {@link Binder}. */
    @NonNull
    public static <T> Mapper<T> singletonMapper(@NonNull Binder<T, ?> binder) {
        //noinspection unchecked
        return new SingletonMapper<>((Binder<T, View>) checkNotNull(binder, "binder"));
    }

    private static final class SingletonMapper<T> extends AbstractMapper<T> {

        @NonNull
        private final Binder<T, View> mBinder;

        @NonNull
        private final Set<Binder<T, View>> mAllBinders;

        SingletonMapper(@NonNull Binder<T, View> binder) {
            mBinder = binder;
            mAllBinders = singleton(mBinder);
        }

        @Nullable
        @Override
        public Binder<T, View> getBinder(@NonNull T item, int position) {
            return mBinder;
        }

        @NonNull
        @Override
        public Collection<? extends Binder<T, ?>> getAllBinders() {
            return mAllBinders;
        }
    }
}
