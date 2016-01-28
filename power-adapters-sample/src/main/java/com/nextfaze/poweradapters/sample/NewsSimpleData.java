package com.nextfaze.poweradapters.sample;

import com.nextfaze.powerdata.ArrayData;
import lombok.NonNull;

import java.util.List;

final class NewsSimpleData extends ArrayData<NewsItem> {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    @Override
    protected List<? extends NewsItem> load() throws Throwable {
        return mNewsService.getNewsFlaky();
    }
}
