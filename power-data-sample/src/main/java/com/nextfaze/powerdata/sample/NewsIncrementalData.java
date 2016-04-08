package com.nextfaze.powerdata.sample;

import android.support.annotation.Nullable;
import com.nextfaze.powerdata.IncrementalArrayData;
import lombok.NonNull;

final class NewsIncrementalData extends IncrementalArrayData<NewsItem> {

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
    protected Result<? extends NewsItem> load() throws Throwable {
        int offset = mOffset;
        if (mOffset >= TOTAL) {
            return null;
        }
        mOffset = offset + INCREMENT;
        return new Result<>(mNewsService.getNews(offset, INCREMENT), TOTAL - mOffset);
    }

    @Override
    protected void onLoadBegin() {
        mOffset = 0;
    }
}
