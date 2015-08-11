package com.nextfaze.poweradapters.sample;

import com.nextfaze.asyncdata.IncrementalArrayData;
import lombok.NonNull;

import javax.annotation.Nullable;

final class NewsIncrementalData extends IncrementalArrayData<Object> {

    private static final int TOTAL = 100;
    private static final int INCREMENT = 20;

    @NonNull
    private final NewsService mNewsService = new NewsService();

    private volatile int mOffset;

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
