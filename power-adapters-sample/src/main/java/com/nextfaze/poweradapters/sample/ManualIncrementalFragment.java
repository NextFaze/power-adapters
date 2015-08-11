package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.nextfaze.poweradapters.PolymorphicMapper;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;

public final class ManualIncrementalFragment extends BaseFragment {

    @NonNull
    private final NewsIncrementalData mData = new NewsIncrementalData();

    @NonNull
    private final Mapper mMapper = new PolymorphicMapper.Builder()
            .bind(NewsItem.class, new NewsItemBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createManualIncrementalAdapter(mData);

    @NonNull
    private LoadNextAdapter createManualIncrementalAdapter(@NonNull Data<?> data) {
        PowerAdapter adapter = new DataBindingAdapter(data, mMapper);
        // Apply a loading adapter to show a loading item as the last item, while data loads more elements.
        adapter = new LoadingAdapter.Builder(adapter, data)
                .loadingItemResource(R.layout.list_loading_item)
                .build();
        // "Load next" adapter lets user click the button to load next increment of results.
        LoadNextAdapter loadNextAdapter = new LoadNextAdapter(data, adapter, R.layout.list_load_next_item);
        loadNextAdapter.setOnClickListener(new LoadNextAdapter.OnLoadNextClickListener() {
            @Override
            public void onClick() {
                onLoadNextClick();
            }
        });
        return loadNextAdapter;
    }

    @Bind(R.id.news_list_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list_fragment_list)
    ListView mListView;

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

    void onLoadNextClick() {
        mData.loadNext();
        scrollToEnd();
    }
}
