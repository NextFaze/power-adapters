package com.nextfaze.databind.sample;

import com.nextfaze.databind.ArrayData;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

final class NewsSimpleData extends ArrayData<Object> {

    @NonNull
    private final NewsService mNewsService;

    NewsSimpleData(@NonNull NewsService newsService) {
        mNewsService = newsService;
    }

    @NonNull
    @Override
    protected List<Object> load() throws Exception {
        ArrayList<Object> data = new ArrayList<>();
        data.add(new NewsSection("Latest News"));
        data.addAll(mNewsService.getNewsFlaky());
        data.add(new NewsSection("Yesterdays News"));
        data.addAll(mNewsService.getNewsFlaky());
        return data;
    }
}
