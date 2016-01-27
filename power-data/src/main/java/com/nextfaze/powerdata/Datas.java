package com.nextfaze.powerdata;

import com.android.internal.util.Predicate;
import lombok.NonNull;

import java.util.Collections;

public final class Datas {

    private Datas() {
    }

    @NonNull
    public static <T> Data<T> emptyData() {
        return new ImmutableData<>(Collections.<T>emptyList());
    }

    /** Filters the specified data based on a predicate. */
    @NonNull
    public static <T> Data<T> filter(@NonNull Data<? extends T> data, @NonNull Predicate<? super T> predicate) {
        return new FilterData<>(data, predicate);
    }

    /** Filter the specified data by class. The resulting elements are guaranteed to be of the given type. */
    @NonNull
    public static <T> Data<T> filter(@NonNull Data<?> data, @NonNull final Class<T> type) {
        //noinspection unchecked
        return (Data<T>) new FilterData<>(data, new Predicate<Object>() {
            @Override
            public boolean apply(Object o) {
                return type.isInstance(o);
            }
        });
    }

    /** Transforms the specified data by applying {@code function} to each element. Does not close the wrapped data. */
    @NonNull
    public static <F, T> Data<T> transform(@NonNull Data<? extends F> data,
                                           @NonNull Function<? super F, ? extends T> function) {
        return new TransformData<>(data, function);
    }

    @NonNull
    public static <T> Data<T> offset(@NonNull Data<? extends T> data, int offset) {
        return new OffsetData<>(data, offset);
    }

    @NonNull
    public static <T> Data<T> limit(@NonNull Data<? extends T> data, int limit) {
        return new LimitData<>(data, limit);
    }

}
