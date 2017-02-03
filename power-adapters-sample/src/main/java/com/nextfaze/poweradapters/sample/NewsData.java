package com.nextfaze.poweradapters.sample;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.data.IncrementalArrayData;

final class NewsData extends IncrementalArrayData<NewsItem> {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    private final int mTotal;
    private final int mIncrement;

    private volatile int mOffset;

    NewsData() {
        this(100, 20);
    }

    NewsData(int total) {
        this(total, total);
    }

    NewsData(int total, int increment) {
        mTotal = total;
        mIncrement = increment;
    }

    @Nullable
    @Override
    protected Result<? extends NewsItem> load() throws Throwable {
        int offset = mOffset;
        if (mOffset >= mTotal) {
            return null;
        }
        mOffset = offset + mIncrement;
        return new Result<>(mNewsService.getNews(offset, mIncrement), mTotal - mOffset);
    }

    @Override
    protected void onLoadBegin() {
        mOffset = 0;
    }
}
