package com.nextfaze.poweradapters.sample;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.nextfaze.poweradapters.DividerAdapterBuilder;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.Predicate;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.BinderWrapper;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import com.nextfaze.poweradapters.data.IncrementalArrayData;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.nextfaze.poweradapters.Condition.not;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.PowerAdapter.concat;
import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.data.DataConditions.data;
import static com.nextfaze.poweradapters.data.DataConditions.isEmpty;
import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public class ConcatFragment extends BaseFragment {

    private static final int ADAPTER_COUNT = 10;

    @NonNull
    private final List<Pair<NewsIncrementalData, PowerAdapter>> mPairs = new ArrayList<>();

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

    public ConcatFragment() {
        Random random = new Random(1);
        for (int i = 0; i < ADAPTER_COUNT; i++) {
            NewsIncrementalData data = new NewsIncrementalData(random.nextInt(10), 3);
            ColoredBinder binder = new ColoredBinder(random.nextInt());
            mPairs.add(createPair(data, binder));
        }
    }

    @NonNull
    private Pair<NewsIncrementalData, PowerAdapter> createPair(@NonNull final NewsIncrementalData data,
                                                               @NonNull Binder newsItemBinder) {
        Binder removeItemBinder = new BinderWrapper(newsItemBinder) {
            @Override
            public void bindView(@NonNull final Object item, @NonNull View v, @NonNull final Holder holder) {
                super.bindView(item, v, holder);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        data.remove(item);
                    }
                });
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showEditDialog(data, holder.getPosition());
                        return true;
                    }
                });
            }
        };
        PowerAdapter adapter = new DataBindingAdapter(data, singletonMapper(removeItemBinder));

        adapter = new DividerAdapterBuilder()
                .innerResource(R.layout.list_divider_item_inner)
                .leadingResource(R.layout.list_divider_item)
                .trailingResource(R.layout.list_divider_item_trailing)
                .emptyPolicy(DividerAdapterBuilder.EmptyPolicy.SHOW_LEADING)
                .build(adapter);

        // Header
        adapter = adapter
                .prepend(asAdapter(R.layout.news_header_item));

        // Loading indicator
        adapter = adapter.compose(appendLoadingIndicator(data));

        data.setLookAheadRowCount(-1);

        // Load next button
        LoadNextAdapter loadNextAdapter =
                new LoadNextAdapter(adapter, data, asViewFactory(R.layout.list_load_next_item));
        loadNextAdapter.setOnClickListener(new LoadNextAdapter.OnLoadNextClickListener() {
            @Override
            public void onClick() {
                data.loadNext();
            }
        });
        adapter = loadNextAdapter;

        // Footer
        adapter = adapter
                .append(asAdapter(R.layout.news_footer_item).showOnlyWhile(not(isEmpty(data))));

        // Empty message
        adapter = adapter
                .append(asAdapter(R.layout.list_empty_item).showOnlyWhile(data(data, new Predicate<Data<?>>() {
                    @Override
                    public boolean apply(Data<?> data) {
                        return data.isEmpty() && !data.isLoading();
                    }
                })));

        return new Pair<NewsIncrementalData, PowerAdapter>(data, adapter);
    }

    private void showEditDialog(@NonNull final IncrementalArrayData<NewsItem> data, final int position) {
        new AlertDialog.Builder(getActivity())
                .setItems(new CharSequence[] {
                        "Add 1 Before",
                        "Add 1 After",
                        "Change 1",
                        "Add 3 Before",
                        "Add 3 After",
                        "Remove all"
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                data.add(position, new NewsItem("Foobar"));
                                break;

                            case 1:
                                data.add(position + 1, new NewsItem("Foobar"));
                                break;

                            case 2:
                                data.set(position, new NewsItem("Changed"));
                                break;

                            case 3:
                                data.addAll(position, Arrays.asList(
                                        new NewsItem("Foobar"),
                                        new NewsItem("Foobar"),
                                        new NewsItem("Foobar")
                                ));
                                break;

                            case 4:
                                data.addAll(position + 1, Arrays.asList(
                                        new NewsItem("Foobar"),
                                        new NewsItem("Foobar"),
                                        new NewsItem("Foobar")
                                ));
                                break;

                            case 5:
                                data.clear();
                                break;
                        }
                    }
                })
                .show();
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
                .transform(new Function<Pair<NewsIncrementalData, PowerAdapter>, Data<?>>() {
                    @Override
                    public Data<?> apply(Pair<NewsIncrementalData, PowerAdapter> pair) {
                        return pair.first;
                    }
                })
                .toList();
        List<PowerAdapter> adapters = FluentIterable.from(mPairs)
                .transform(new Function<Pair<NewsIncrementalData, PowerAdapter>, PowerAdapter>() {
                    @Override
                    public PowerAdapter apply(Pair<NewsIncrementalData, PowerAdapter> pair) {
                        return pair.second;
                    }
                })
                .toList();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(toRecyclerAdapter(concat(adapters)));
        mDataLayout.setDatas(datas);
        showCollectionView(CollectionView.RECYCLER_VIEW);
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
}
