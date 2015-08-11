package com.nextfaze.poweradapters.sample;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.TypedBinder;
import lombok.NonNull;

final class NewsItemBinder extends TypedBinder<NewsItem, TextView> {

    NewsItemBinder() {
        super(android.R.layout.simple_list_item_1);
    }

    @Override
    protected void bind(@NonNull final NewsItem newsItem, @NonNull final TextView v, @NonNull Holder holder) {
        v.setText(newsItem.getTitle());
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNewsItemClick(newsItem, v);
            }
        });
    }

    private void onNewsItemClick(@NonNull NewsItem newsItem, @NonNull View v) {
        Toast.makeText(v.getContext(), "News item clicked: " + newsItem.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
