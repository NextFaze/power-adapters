package com.nextfaze.poweradapters.sample.news;

import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;

import androidx.annotation.NonNull;

public final class News {
    @NonNull
    static PowerAdapter createNewsAdapter(@NonNull Data<NewsItem> data) {
        return new DataBindingAdapter<>(new NewsItemBinder(data.asList()), data);
    }
}
