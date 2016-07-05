package com.nextfaze.poweradapters;

import android.support.annotation.CheckResult;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

public final class PowerAdapters {

    private static final WeakMap<PowerAdapter, ListAdapterConverterAdapter> sListConverterAdapters = new WeakMap<>();

    private PowerAdapters() {
    }

    /**
     * Returns the specified {@link PowerAdapter} as a {@link ListAdapter}.
     * @param powerAdapter The adapter to be converted.
     * @return A list adapter that presents the same views as {@code powerAdapter}.
     * @see ListView#setAdapter(ListAdapter)
     */
    @CheckResult
    @NonNull
    public static ListAdapter toListAdapter(@NonNull PowerAdapter powerAdapter) {
        ListAdapterConverterAdapter converterAdapter = sListConverterAdapters.get(powerAdapter);
        if (converterAdapter == null) {
            // HACK: We simply have to use a magic number here and hope we never exceed it.
            // ListAdapter interface gives us no other choice, since it requires knowledge of all possible view types
            // in advance, and a PowerAdapter cannot provide this.
            // In practise, this means wastefully creating X ArrayLists in AbsListView, which isn't much compared to
            // the memory overhead of a single View.
            converterAdapter = new ListAdapterConverterAdapter(powerAdapter, 50);
            sListConverterAdapters.put(powerAdapter, converterAdapter);
        }
        return converterAdapter;
    }

    /**
     * Returns the specified {@link PowerAdapter} as a {@link SpinnerAdapter}.
     * <p>
     * <strong>Note that the supplied adapter must only ever use a single view type</strong>, due to constraints
     * imposed by {@link Spinner#setAdapter(SpinnerAdapter)}.
     * @param powerAdapter The adapter to be converted.
     * @return A spinner adapter that presents the same views as {@code powerAdapter}.
     * @see Spinner#setAdapter(SpinnerAdapter)
     * @see PowerAdapter#getItemViewType(int)
     */
    @CheckResult
    @NonNull
    public static SpinnerAdapter toSpinnerAdapter(@NonNull PowerAdapter powerAdapter) {
        // SpinnerAdapter adds additional constraints to the ListAdapter contract: getViewTypeCount must return 1.
        // See android.widget.Spinner.setAdapter()
        return new ListAdapterConverterAdapter(powerAdapter, 1);
    }
}
