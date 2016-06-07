package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

final class AdapterTestUtils {

    AdapterTestUtils() {
    }

    @NonNull
    static Holder holder(final int position) {
        return new TestHolder(position);
    }

    static void verifyNewViewNeverCalled(@NonNull PowerAdapter adapter) {
        verify(adapter, never()).newView(any(ViewGroup.class), any(ViewType.class));
    }

    static void verifyBindViewNeverCalled(@NonNull PowerAdapter adapter) {
        verify(adapter, never()).bindView(any(View.class), any(Holder.class));
    }
}
