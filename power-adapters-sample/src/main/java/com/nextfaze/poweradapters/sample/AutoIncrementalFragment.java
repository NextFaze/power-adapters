package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListView;
import butterknife.Bind;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public final class AutoIncrementalFragment extends BaseFragment {

    @NonNull
    private final NewsIncrementalData mData = new NewsIncrementalData(50, 10);

    @NonNull
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createAutoIncrementalAdapter(mData);

    @NonNull
    private PowerAdapter createAutoIncrementalAdapter(@NonNull Data<?> data) {
        return new DataBindingAdapter(data, mMapper).compose(appendLoadingIndicator(data));
    }

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.list)
    ListView mListView;

    @Bind(R.id.recycler)
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
//        mListView.setAdapter(toListAdapter(mAdapter));
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
}
