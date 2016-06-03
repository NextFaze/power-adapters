package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import lombok.NonNull;

import static com.nextfaze.poweradapters.sample.Utils.showEditDialog;

class NewsItemBinder extends AbstractBinder<NewsItem, NewsItemView> {

    @NonNull
    private final NewsIncrementalData mData;

    NewsItemBinder(@NonNull NewsIncrementalData data) {
        super(R.layout.news_item_binder);
        mData = data;
    }

    @Override
    public void bindView(@NonNull NewsItem newsItem, @NonNull NewsItemView v, @NonNull Holder holder) {
        v.setNewsItem(newsItem);
        v.setOnClickListener(v1 -> onNewsItemClick(newsItem, v, holder.getPosition()));
        v.setRemoveOnClickListener(v1 -> mData.remove(holder.getPosition()));
        v.setInsertBeforeOnClickListener(v1 -> onInsertBeforeClick(newsItem, v, holder.getPosition()));
        v.setInsertAfterOnClickListener(v1 -> onInsertAfterClick(newsItem, v, holder.getPosition()));
    }

    private void onNewsItemClick(@NonNull NewsItem newsItem, @NonNull NewsItemView v, int position) {
        showEditDialog(v.getContext(), mData, position);
    }

    private void onInsertBeforeClick(@NonNull NewsItem newsItem, @NonNull NewsItemView newsItemView, int position) {
        if (newsItemView.isMultipleChecked()) {
            mData.addAll(position, NewsItem.create(5));
        } else {
            mData.add(position, new NewsItem());
        }
    }

    private void onInsertAfterClick(@NonNull NewsItem newsItem, @NonNull NewsItemView newsItemView, int position) {
        if (newsItemView.isMultipleChecked()) {
            mData.addAll(position + 1, NewsItem.create(5));
        } else {
            mData.add(position + 1, new NewsItem());
        }
    }
}
