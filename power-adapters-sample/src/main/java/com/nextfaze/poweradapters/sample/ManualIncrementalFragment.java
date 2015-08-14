package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.Bind;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.LoadingAdapter;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.DataBindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import com.nextfaze.poweradapters.data.DataLoadingDelegate;
import lombok.NonNull;

import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public final class ManualIncrementalFragment extends BaseFragment {

    @NonNull
    private final NewsIncrementalData mData = new NewsIncrementalData(100, 5);

    @NonNull
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder() {
                @Override
                void onNewsItemClick(@NonNull NewsItem newsItem, @NonNull View v) {
                    mData.remove(newsItem);
                }
            })
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createManualIncrementalAdapter(mData);

    @NonNull
    private PowerAdapter createManualIncrementalAdapter(@NonNull Data<?> data) {
        PowerAdapter adapter = new DataBindingAdapter(data, mMapper);
        // Apply a loading adapter to show a loading item as the last item, while data loads more elements.
        adapter = new LoadingAdapter.Builder(adapter, new DataLoadingDelegate(data))
                .loadingItemResource(R.layout.list_loading_item)
                .build();
        // "Load next" adapter lets user click the button to load next increment of results.
        LoadNextAdapter loadNextAdapter = new LoadNextAdapter(adapter, data, R.layout.list_load_next_item);
        loadNextAdapter.setOnClickListener(new LoadNextAdapter.OnLoadNextClickListener() {
            @Override
            public void onClick() {
                onLoadNextClick();
            }
        });
        adapter = loadNextAdapter;
        return adapter;
    }

    @Bind(R.id.news_list_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list_fragment_recycler)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        // Never pre-load; let the user click 'load next' instead.
        mData.setLookAheadRowCount(-1);
    }

    @Override
    public void onDestroy() {
        mData.close();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setAdapter(toRecyclerAdapter(mAdapter));
        mDataLayout.setData(mData);
        showCollectionView(CollectionView.RECYCLER_VIEW);
    }

    @Override
    void onReloadClick() {
        mData.reload();
    }

    @Override
    void onRefreshClick() {
        mData.refresh();
    }

    @Override
    void onInvalidateClick() {
        mData.invalidate();
        showToast("Data invalidated; background the app or change orientation to trigger reload");
    }

    void onLoadNextClick() {
        mData.loadNext();
        scrollToEnd();
    }
}
