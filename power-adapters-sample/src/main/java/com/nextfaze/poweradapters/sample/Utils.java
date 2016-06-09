package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import java.util.List;

import static com.nextfaze.poweradapters.Condition.not;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.data.DataConditions.*;

@SuppressWarnings("Guava")
final class Utils {

    private Utils() {
    }

    @NonNull
    static PowerAdapter createNewsAdapter(@NonNull NewsData data) {
        return new DataBindingAdapter(data, new NewsItemBinder(data));
    }

    @NonNull
    static PowerAdapter emptyMessage(@NonNull Data<?> data) {
        return asAdapter(R.layout.list_empty_item).showOnlyWhile(isEmpty(data).and(not(isLoading(data))));
    }

    @NonNull
    static PowerAdapter loadingIndicator(@NonNull Data<?> data) {
        return asAdapter(R.layout.list_loading_item).showOnlyWhile(data(data, d -> d.isLoading() && !d.isEmpty()));
    }

    @NonNull
    static PowerAdapter loadNextButton(@NonNull Data<?> data,
                                       @NonNull View.OnClickListener onClickListener) {
        ViewFactory loadNextButton = parent -> {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_load_next_item, parent, false);
            v.setOnClickListener(onClickListener);
            return v;
        };
        Condition dataHasMoreAvailable = data(data, d -> !d.isLoading() && !d.isEmpty() && d.available() > 0);
        return asAdapter(loadNextButton).showOnlyWhile(dataHasMoreAvailable);
    }

    static void showEditDialog(@NonNull Context context, @NonNull List<? super Object> data, int position) {
        List<Item> items = ImmutableList.of(
                new Item("Change", v -> data.set(position, NewsItem.create("Changed"))),
                new Item("Clear", v -> data.clear())
        );
        String[] itemTitles = FluentIterable.from(items).transform(item -> item.title).toArray(String.class);
        new AlertDialog.Builder(context)
                .setItems(itemTitles, (dialog, which) -> items.get(which).onClickListener.onClick(null))
                .show();
    }

    static class Item {
        @NonNull
        final String title;

        @NonNull
        final View.OnClickListener onClickListener;

        Item(@NonNull String title, @NonNull View.OnClickListener onClickListener) {
            this.title = title;
            this.onClickListener = onClickListener;
        }
    }
}
