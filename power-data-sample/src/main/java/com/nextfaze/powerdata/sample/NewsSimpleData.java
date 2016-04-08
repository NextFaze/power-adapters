package com.nextfaze.powerdata.sample;

import com.nextfaze.powerdata.ArrayData;
import lombok.NonNull;

import java.util.List;

final class NewsSimpleData extends ArrayData<NewsItem> {

    @NonNull
    private final NewsService mNewsService;

    NewsSimpleData(@NonNull NewsService newsService) {
        mNewsService = newsService;
    }

    @NonNull
    @Override
    protected List<NewsItem> load() throws Exception {
        return mNewsService.getNews(0, 10);
    }
}
