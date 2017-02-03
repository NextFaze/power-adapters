package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.Predicate;

public final class DataConditions {

    private DataConditions() {
    }

    @NonNull
    public static <T> Condition data(@NonNull Data<? extends T> data,
                                     @NonNull Predicate<? super Data<? extends T>> predicate) {
        return new DataCondition<>(data, predicate);
    }

    @NonNull
    public static Condition isEmpty(@NonNull Data<?> data) {
        return data(data, new Predicate<Data<?>>() {
            @Override
            public boolean apply(Data<?> data) {
                return data.isEmpty();
            }
        });
    }

    @NonNull
    public static Condition isNotEmpty(@NonNull Data<?> data) {
        return data(data, new Predicate<Data<?>>() {
            @Override
            public boolean apply(Data<?> data) {
                return !data.isEmpty();
            }
        });
    }

    @NonNull
    public static Condition isLoading(@NonNull Data<?> data) {
        return data(data, new Predicate<Data<?>>() {
            @Override
            public boolean apply(Data<?> data) {
                return data.isLoading();
            }
        });
    }

    @NonNull
    public static Condition isNotLoading(@NonNull Data<?> data) {
        return data(data, new Predicate<Data<?>>() {
            @Override
            public boolean apply(Data<?> data) {
                return !data.isLoading();
            }
        });
    }

    @NonNull
    public static Condition hasAvailableElements(@NonNull Data<?> data) {
        return data(data, new Predicate<Data<?>>() {
            @Override
            public boolean apply(Data<?> data) {
                return data.available() > 0;
            }
        });
    }

    @NonNull
    public static Condition hasNoAvailableElements(@NonNull Data<?> data) {
        return data(data, new Predicate<Data<?>>() {
            @Override
            public boolean apply(Data<?> data) {
                return data.available() == 0;
            }
        });
    }
}
