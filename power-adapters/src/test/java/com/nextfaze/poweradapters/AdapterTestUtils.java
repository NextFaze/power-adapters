package com.nextfaze.poweradapters;

import lombok.NonNull;

final class AdapterTestUtils {

    AdapterTestUtils() {
    }

    @NonNull
    static FakeAdapter<Integer> fakeIntAdapter(int itemCount) {
        FakeAdapter<Integer> adapter = new FakeAdapter<>();
        for (int i = 0; i < itemCount; i++) {
            adapter.add(i);
        }
        return adapter;
    }
}
