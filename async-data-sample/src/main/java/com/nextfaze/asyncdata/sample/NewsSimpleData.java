package com.nextfaze.asyncdata.sample;

import com.nextfaze.asyncdata.ArrayData;
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
        return mNewsService.getNewsFlaky();
    }
}
