package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.widget.ListAdapter;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

import java.util.Collection;

import static com.nextfaze.poweradapters.Item.toItems;
import static com.nextfaze.poweradapters.PowerAdapter.EMPTY;
import static java.util.Arrays.asList;

public final class PowerAdapters {

    private static final WeakMap<PowerAdapter, ListAdapterConverterAdapter> sListConverterAdapters = new WeakMap<>();

    private PowerAdapters() {
    }

    @CheckResult
    @NonNull
    public static ListAdapter toListAdapter(@NonNull PowerAdapter powerAdapter) {
        ListAdapterConverterAdapter converterAdapter = sListConverterAdapters.get(powerAdapter);
        if (converterAdapter == null) {
            converterAdapter = new ListAdapterConverterAdapter(powerAdapter);
            sListConverterAdapters.put(powerAdapter, converterAdapter);
        }
        return converterAdapter;
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull PowerAdapter... powerAdapters) {
        if (powerAdapters.length == 1) {
            return powerAdapters[0];
        }
        return new ConcatAdapter(asList(powerAdapters));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Iterable<? extends PowerAdapter> powerAdapters) {
        return new ConcatAdapter(powerAdapters);
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull ViewFactory... views) {
        if (views.length == 0) {
            return EMPTY;
        }
        return new ItemAdapter(toItems(asList(views)));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull Iterable<? extends ViewFactory> views) {
        return new ItemAdapter(toItems(views));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull Collection<? extends ViewFactory> views) {
        if (views.isEmpty()) {
            return EMPTY;
        }
        return new ItemAdapter(toItems(views));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull @LayoutRes int... resources) {
        return new ItemAdapter(toItems(resources));
    }

    @NonNull
    public static PowerAdapter showOnlyWhile(@NonNull PowerAdapter adapter,
                                             @NonNull Condition condition) {
        return new ConditionalAdapter(adapter, condition);
    }
}
