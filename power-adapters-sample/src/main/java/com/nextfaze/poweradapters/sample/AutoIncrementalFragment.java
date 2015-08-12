package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import butterknife.Bind;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.DataBindingAdapter;
import com.nextfaze.poweradapters.LoadingAdapter;
import com.nextfaze.poweradapters.Mapper;
import com.nextfaze.poweradapters.PolymorphicMapperBuilder;
import com.nextfaze.poweradapters.PowerAdapter;
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

    @Bind(R.id.news_list_fragment_recycler)
    RecyclerView mRecyclerView;

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Deferred Invalidate").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onInvalidateDeferredClick();
                return true;
            }
        });
    }

    @Override
    void onClearClick() {
        mData.clear();
    }

    @Override
    void onInvalidateClick() {
        mData.invalidate();
    }

    void onInvalidateDeferredClick() {
        mData.invalidateDeferred();
        showToast("Data invalidated; background the app or change orientation to trigger reload");
    }
}
