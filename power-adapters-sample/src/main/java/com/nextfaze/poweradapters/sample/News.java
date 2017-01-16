package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

public final class News {
    @NonNull
    static PowerAdapter createNewsAdapter(@NonNull Data<NewsItem> data) {
        return new DataBindingAdapter<>(new NewsItemBinder(data.asList()), data);
    }
}
