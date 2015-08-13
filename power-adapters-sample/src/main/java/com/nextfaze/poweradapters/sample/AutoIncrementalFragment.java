package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import butterknife.Bind;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.LoadingAdapter;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.DataBindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;

public final class AutoIncrementalFragment extends BaseFragment {

    @NonNull
    private final NewsIncrementalData mData = new NewsIncrementalData();

    @NonNull
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createAutoIncrementalAdapter(mData);

    @NonNull
    private PowerAdapter createAutoIncrementalAdapter(@NonNull Data<?> data) {
        PowerAdapter adapter = new DataBindingAdapter(data, mMapper);
        // Apply a loading adapter to show a loading item as the last item, while data loads more elements.
        adapter = new LoadingAdapter.Builder(adapter, data)
                .loadingItemResource(R.layout.list_loading_item)
                .build();
        return adapter;
    }

    @Bind(R.id.news_list_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list_fragment_list)
    ListView mListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        // Start pre-loading as user approaches end of loaded content.
        mData.setLookAheadRowCount(10);
    }

    @Override
    public void onDestroy() {
        mData.close();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView.setAdapter(toListAdapter(mAdapter));
        mDataLayout.setData(mData);
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
}
