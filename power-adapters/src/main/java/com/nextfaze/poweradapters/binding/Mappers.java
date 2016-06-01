package com.nextfaze.poweradapters.binding;

import android.support.annotation.Nullable;
import lombok.NonNull;

import java.util.Collection;

import static java.util.Collections.singleton;

public final class Mappers {

    private Mappers() {
    }

    /** Creates a {@link Mapper} that only ever uses the single supplied {@link Binder}. */
    @NonNull
    public static Mapper singletonMapper(@NonNull Binder<?, ?> binder) {
        return new SingletonMapper(binder);
    }

    private static class SingletonMapper extends AbstractMapper {

        @NonNull
        private final Binder<?, ?> mBinder;

        SingletonMapper(@NonNull Binder<?, ?> binder) {
            mBinder = binder;
        }

        @Nullable
        @Override
        public Binder<?, ?> getBinder(@NonNull Object item, int position) {
            return mBinder;
        }

        @NonNull
        @Override
        public Collection<? extends Binder<?, ?>> getAllBinders() {
            return singleton(mBinder);
        }
    }
}
