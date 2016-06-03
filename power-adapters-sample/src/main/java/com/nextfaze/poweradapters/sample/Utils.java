package com.nextfaze.poweradapters.sample;

import android.view.LayoutInflater;
import android.view.View;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.data.Data;
import lombok.NonNull;

import static com.nextfaze.poweradapters.Condition.not;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.data.DataConditions.*;

final class Utils {

    private Utils() {
    }

    @NonNull
    static PowerAdapter.Transformer appendLoadingIndicator(@NonNull Data<?> data) {
        return adapter -> adapter.append(asAdapter(R.layout.list_loading_item)
                .showOnlyWhile(data(data, d -> d.isLoading() && !d.isEmpty())));
    }

    @NonNull
    static PowerAdapter.Transformer appendEmptyMessage(@NonNull Data<?> data) {
        return adapter -> adapter.append(asAdapter(R.layout.list_empty_item)
                .showOnlyWhile(isEmpty(data).and(not(isLoading(data)))));
    }

    @NonNull
    static PowerAdapter.Transformer appendLoadNextButton(@NonNull Data<?> data,
                                                         @NonNull View.OnClickListener onClickListener) {
        ViewFactory loadNextButton = parent -> {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_load_next_item, parent, false);
            v.setOnClickListener(onClickListener);
            return v;
        };
        Condition dataHasMoreAvailable = data(data, d -> !d.isLoading() && !d.isEmpty() && d.available() > 0);
        return adapter -> adapter.append(asAdapter(loadNextButton).showOnlyWhile(dataHasMoreAvailable));
    }
}
