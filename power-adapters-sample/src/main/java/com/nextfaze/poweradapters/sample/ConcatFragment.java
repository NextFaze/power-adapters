package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.View;
import butterknife.ButterKnife;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.nextfaze.poweradapters.Condition.not;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.PowerAdapter.concat;
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.data.DataConditions.isEmpty;
import static com.nextfaze.poweradapters.sample.Utils.*;

public final class ConcatFragment extends BaseFragment {

    private static final Random RANDOM = new Random(3);

    private static final int ADAPTER_COUNT = 10;

    @NonNull
    private final List<Pair<NewsIncrementalData, PowerAdapter>> mPairs = new ArrayList<>();

    public ConcatFragment() {
        for (int i = 0; i < ADAPTER_COUNT; i++) {
            NewsIncrementalData data = new NewsIncrementalData(15, 5);
            mPairs.add(createPair(data, new ColoredBinder(data, RANDOM.nextInt())));
        }
    }

    @NonNull
    private Pair<NewsIncrementalData, PowerAdapter> createPair(@NonNull NewsIncrementalData data,
                                                               @NonNull NewsItemBinder newsItemBinder) {
        PowerAdapter adapter = new DataBindingAdapter(data, singletonMapper(newsItemBinder));

        // Header
        adapter = adapter.prepend(asAdapter(R.layout.news_header_item));

        // Loading indicator
        adapter = adapter.compose(appendLoadingIndicator(data));

        data.setLookAheadRowCount(-1);

        // Load next button
        adapter = adapter.compose(appendLoadNextButton(data, v -> data.loadNext()));

        // Footer
        adapter = adapter.append(asAdapter(R.layout.news_footer_item).showOnlyWhile(not(isEmpty(data))));

        // Empty message
        adapter = adapter.compose(appendEmptyMessage(data));

        return new Pair<>(data, adapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        for (Pair<NewsIncrementalData, PowerAdapter> pair : mPairs) {
            pair.first.close();
        }
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        List<Data<?>> datas = FluentIterable.from(mPairs)
                .transform((Function<Pair<NewsIncrementalData, PowerAdapter>, Data<?>>) pair -> pair.first)
                .toList();
        List<PowerAdapter> adapters = FluentIterable.from(mPairs)
                .transform(pair -> pair.second)
                .toList();
        setAdapter(concat(adapters));
        mDataLayout.setDatas(datas);
    }

    private static final class ColoredBinder extends NewsItemBinder {

        private final int mColor;

        ColoredBinder(@NonNull NewsIncrementalData data, int color) {
            super(data);
            mColor = color;
        }

        @Override
        public void bindView(@NonNull NewsItem newsItem, @NonNull NewsItemView v, @NonNull Holder holder) {
            super.bindView(newsItem, v, holder);
            v.setBackgroundColor(mColor);
        }
    }
}
