package com.nextfaze.poweradapters.sample;

import com.nextfaze.asyncdata.IncrementalArrayData;
import lombok.NonNull;

import javax.annotation.Nullable;

final class NewsIncrementalData extends IncrementalArrayData<Object> {

    private static final int TOTAL = 30;
    private static final int INCREMENT = 10;

    @NonNull
    private final NewsService mNewsService;

    private volatile int mOffset;

    NewsIncrementalData(@NonNull NewsService newsService) {
        mNewsService = newsService;
    }

    @Nullable
    @Override
    protected Result<?> load() throws Throwable {
        int offset = mOffset;
        if (mOffset >= TOTAL) {
            return null;
        }
        mOffset = offset + INCREMENT;
        return new Result<>(mNewsService.getNews(offset, INCREMENT), TOTAL - mOffset);
    }

    @Override
    protected void onInvalidate() {
        mOffset = 0;
    }
}
