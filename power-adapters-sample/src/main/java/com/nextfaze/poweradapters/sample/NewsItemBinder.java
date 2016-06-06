package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import lombok.NonNull;

import java.util.List;

import static com.nextfaze.poweradapters.sample.Utils.showEditDialog;
import static java.util.Collections.emptySet;

class NewsItemBinder extends AbstractBinder<NewsItem, NewsItemView> {

    @NonNull
    private final List<Object> mList;

    NewsItemBinder(@NonNull List<Object> list) {
        super(R.layout.news_item_binder);
        mList = list;
    }

    @Override
    public void bindView(@NonNull NewsItem newsItem, @NonNull NewsItemView v, @NonNull Holder holder) {
        v.setNewsItem(newsItem);
        v.setTags(emptySet());
        v.setOnClickListener(v1 -> onNewsItemClick(newsItem, v, holder.getPosition()));
        v.setRemoveOnClickListener(v1 -> mList.remove(holder.getPosition()));
        v.setInsertBeforeOnClickListener(v1 -> onInsertBeforeClick(newsItem, v, holder.getPosition()));
        v.setInsertAfterOnClickListener(v1 -> onInsertAfterClick(newsItem, v, holder.getPosition()));
    }

    private void onNewsItemClick(@NonNull NewsItem newsItem, @NonNull NewsItemView v, int position) {
        showEditDialog(v.getContext(), mList, position);
    }

    private void onInsertBeforeClick(@NonNull NewsItem newsItem, @NonNull NewsItemView newsItemView, int position) {
        if (newsItemView.isMultipleChecked()) {
            mList.addAll(position, BlogPost.create(5));
        } else {
            mList.add(position, BlogPost.create());
        }
    }

    private void onInsertAfterClick(@NonNull NewsItem newsItem, @NonNull NewsItemView newsItemView, int position) {
        if (newsItemView.isMultipleChecked()) {
            mList.addAll(position + 1, BlogPost.create(5));
        } else {
            mList.add(position + 1, BlogPost.create());
        }
    }
}
