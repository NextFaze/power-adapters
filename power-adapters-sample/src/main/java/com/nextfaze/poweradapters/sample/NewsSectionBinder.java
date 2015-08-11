package com.nextfaze.poweradapters.sample;

import android.graphics.Typeface;
import android.widget.TextView;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.TypedBinder;
import lombok.NonNull;

final class NewsSectionBinder extends TypedBinder<NewsSection, TextView> {

    NewsSectionBinder() {
        super(android.R.layout.simple_list_item_1, false);
    }

    @Override
    protected void bind(@NonNull NewsSection newsSection, @NonNull TextView textView, @NonNull Holder holder) {
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setText(newsSection.getTitle());
    }
}
