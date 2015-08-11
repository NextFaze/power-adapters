package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import butterknife.Bind;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.DataBindingAdapter;
import com.nextfaze.poweradapters.Mapper;
import com.nextfaze.poweradapters.PolymorphicMapper;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public final class LongLivedDataFragment extends BaseFragment {

    @NonNull
    private final Mapper mMapper = new PolymorphicMapper.Builder()
            .bind(NewsItem.class, new NewsItemBinder())
            .build();

    @NonNull
    private NewsSimpleData mData;

    @NonNull
    private PowerAdapter mAdapter;

    @Bind(R.id.news_list_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list_fragment_list)
    ListView mListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mData = getSampleApplication().getLongLivedData();
        mAdapter = new DataBindingAdapter(mData, mMapper);
    }

    @Override
    public void onDestroy() {
//        mData.close();
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
    void onClearClick() {
        mData.clear();
    }

    @Override
    void onInvalidateClick() {
        mData.invalidate();
    }
}
