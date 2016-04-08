package com.nextfaze.powerdata.sample;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.String.format;

final class NewsService {

    private static final Random RANDOM = new Random();

    @NonNull
    List<NewsItem> getNewsFlaky() throws Exception {
        Thread.sleep(1000);
        ArrayList<NewsItem> newsItems = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            newsItems.add(new NewsItem(format("News Item #%s", i)));
        }
        if (RANDOM.nextInt(5) == 0) {
            throw new RuntimeException("Random failure");
        }
        return newsItems;
    }

    @NonNull
    List<NewsItem> getNews(int offset, int count) throws Exception {
        Thread.sleep(2000);
        ArrayList<NewsItem> newsItems = new ArrayList<>();
        for (int i = offset; i < offset + count; ++i) {
            newsItems.add(new NewsItem(format("News Item #%s", i)));
        }
        return newsItems;
    }
}
