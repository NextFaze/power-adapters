package com.nextfaze.poweradapters.internal;

import android.support.annotation.Nullable;
import lombok.NonNull;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/** Weak hash map with a weak value as well as key, which means values can hold strong references to their key. */
public final class WeakMap<K, V> {

    @NonNull
    private final WeakHashMap<K, WeakReference<V>> mMap = new WeakHashMap<>();

    @Nullable
    public V get(@NonNull K k) {
        WeakReference<V> ref = mMap.get(k);
        if (ref == null) {
            return null;
        }
        return ref.get();
    }

    public void put(@NonNull K k, @NonNull V v) {
        mMap.put(k, new WeakReference<>(v));
    }
}
