package com.nextfaze.databind.sample;

import com.nextfaze.databind.IncrementalArrayData;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;

final class NewsIncrementalData extends IncrementalArrayData<Object> {

    private static final int TOTAL = 50;
    private static final int INCREMENT = 10;

    @NonNull
    private final NewsService mNewsService;

    private volatile int mOffset;

    NewsIncrementalData(@NonNull NewsService newsService) {
        mNewsService = newsService;
    }

    @Nullable
    @Override
    protected List<?> load() throws Throwable {
        int offset = mOffset;
        if (offset >= TOTAL) {
            return null;
        }
        mOffset = offset + INCREMENT;
        return mNewsService.getNews(offset, INCREMENT);
    }

    @Override
    protected void onInvalidate() {
        mOffset = 0;
    }
}
