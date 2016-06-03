package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.data.ArrayData;
import lombok.NonNull;

import java.util.List;

final class NewsSimpleData extends ArrayData<NewsItem> {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    @Override
    protected List<? extends NewsItem> load() throws Throwable {
        return mNewsService.getNews();
    }
}
