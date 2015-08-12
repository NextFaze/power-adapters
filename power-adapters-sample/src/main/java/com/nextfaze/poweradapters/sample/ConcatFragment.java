package com.nextfaze.poweradapters.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.LoadingAdapter;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.DataBindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import lombok.NonNull;

import java.util.List;

import static com.nextfaze.poweradapters.PowerAdapters.concat;
import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public class ConcatFragment extends Fragment {

    @NonNull
    private final Binder mRedBinder = new ColoredBinder(Color.RED);

    @NonNull
    private final Binder mGreenBinder = new ColoredBinder(Color.GREEN);

    @NonNull
    private final Binder mBlueBinder = new ColoredBinder(Color.BLUE);

    @NonNull
    private final List<? extends Pair<Data<?>, PowerAdapter>> mPairs = ImmutableList.of(
            createPair(new NewsIncrementalData(5, 2), mRedBinder),
            createPair(new NewsIncrementalData(10, 5), mGreenBinder),
            createPair(new NewsIncrementalData(20, 5), mBlueBinder)
    );

    @NonNull
    private Pair<Data<?>, PowerAdapter> createPair(@NonNull Data<?> data, @NonNull Binder newsItemBinder) {
        Mapper mapper = new PolymorphicMapperBuilder()
                .bind(NewsItem.class, newsItemBinder)
                .build();
        PowerAdapter adapter = new DataBindingAdapter(data, mapper);
        adapter = new LoadingAdapter.Builder(adapter, data)
                .loadingItemResource(R.layout.list_loading_item)
                .build();
        return new Pair<Data<?>, PowerAdapter>(data, adapter);
    }

    @Bind(R.id.news_recycler)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        for (Pair<Data<?>, PowerAdapter> pair : mPairs) {
            pair.first.close();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_recycler, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        List<PowerAdapter> adapters = FluentIterable.from(mPairs)
                .transform(new Function<Pair<Data<?>, PowerAdapter>, PowerAdapter>() {
                    @Override
                    public PowerAdapter apply(Pair<Data<?>, PowerAdapter> pair) {
                        return pair.second;
                    }
                })
                .toList();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(toRecyclerAdapter(concat(adapters)));
    }

    /**
     * @see BaseFragment#onDestroyView()
     */
    @Override
    public void onDestroyView() {
        mRecyclerView.setAdapter(null);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Pair<Data<?>, PowerAdapter> pair : mPairs) {
            pair.first.notifyShown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (Pair<Data<?>, PowerAdapter> pair : mPairs) {
            pair.first.notifyHidden();
        }
    }

    static final class ColoredBinder extends NewsItemBinder {

        private final int mColor;

        ColoredBinder(int color) {
            mColor = color;
        }

        @Override
        protected void bind(@NonNull NewsItem newsItem, @NonNull TextView v, @NonNull Holder holder) {
            super.bind(newsItem, v, holder);
            v.setBackgroundColor(mColor);
        }
    }

    // TODO: Concat several Data adapters.
    // TODO: Use an EmptyAdapter.

}
