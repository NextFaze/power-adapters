package com.nextfaze.poweradapters.sample.news;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.binding.Binder;

public final class NewsSectionBinder extends Binder<NewsSection, TextView> {

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    public void bindView(@NonNull Container container,
                         @NonNull NewsSection newsSection,
                         @NonNull TextView v,
                         @NonNull Holder holder) {
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setText(newsSection.getTitle());
    }

    @Override
    public boolean isEnabled(@NonNull NewsSection newsSection, int position) {
        return false;
    }
}
