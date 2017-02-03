package com.nextfaze.poweradapters.sample;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.widget.TextView;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.binding.AbstractBinder;

final class NewsSectionBinder extends AbstractBinder<NewsSection, TextView> {

    NewsSectionBinder() {
        super(android.R.layout.simple_list_item_1);
    }

    @Override
    public void bindView(@NonNull NewsSection newsSection, @NonNull TextView v, @NonNull Holder holder) {
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setText(newsSection.getTitle());
    }

    @Override
    public boolean isEnabled(@NonNull NewsSection newsSection, int position) {
        return false;
    }
}
