package com.nextfaze.asyncdata.widget;

import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

final class SetUtils {
    @NonNull
    static <T> Set<T> newHashSet(@NonNull Iterable<? extends T> iterable) {
        Set<T> set = new HashSet<>();
        for (T t : iterable) {
            set.add(t);
        }
        return set;
    }

    @NonNull
    static <T> Set<T> symmetricDifference(@NonNull Set<? extends T> a, @NonNull Set<? extends T> b) {
        HashSet<T> diff = new HashSet<>();
        for (T t : a) {
            if (a.contains(t) && !b.contains(t)) {
                diff.add(t);
            }
        }
        for (T t : b) {
            if (b.contains(t) && !a.contains(t)) {
                diff.add(t);
            }
        }
        return diff;
    }
}
