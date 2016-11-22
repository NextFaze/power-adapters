package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

public final class News {
    @NonNull
    static PowerAdapter createNewsAdapter(@NonNull NewsData data) {
        return new DataBindingAdapter<>(data, new NewsItemBinder(data.asList()));
    }
}
