package com.nextfaze.poweradapters.sample;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import lombok.NonNull;

class NewsItemBinder extends AbstractBinder<NewsItem, TextView> {

    NewsItemBinder() {
        super(android.R.layout.simple_list_item_1);
    }

    @Override
    public void bindView(@NonNull final NewsItem newsItem, @NonNull TextView v, @NonNull Holder holder) {
        v.setText(newsItem.getTitle());
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewsItemClick(newsItem, v);
            }
        });
    }

    void onNewsItemClick(@NonNull NewsItem newsItem, @NonNull View v) {
        Toast.makeText(v.getContext(), "News item clicked: " + newsItem.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
