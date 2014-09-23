package com.nextfaze.databind.sample;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.String.format;

public class NewsService {

    private static final Random RANDOM = new Random();

    @NonNull
    List<NewsItem> getNews() throws Exception {
        Thread.sleep(1000);
        ArrayList<NewsItem> newsItems = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            newsItems.add(new NewsItem(format("News Item #%s", i)));
        }
        if (RANDOM.nextInt(3) == 0) {
//            throw new RuntimeException("Randomly induced failure");
        }
        return newsItems;
    }
}
