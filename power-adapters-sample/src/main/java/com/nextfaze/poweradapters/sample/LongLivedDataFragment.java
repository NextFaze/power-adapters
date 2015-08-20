package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import butterknife.Bind;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.asyncdata.DataBindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import lombok.NonNull;

import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public final class LongLivedDataFragment extends BaseFragment {

    @NonNull
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder())
            .build();

    private NewsSimpleData mData;

    private PowerAdapter mAdapter;

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mData = getSampleApplication().getLongLivedData();
        mAdapter = new DataBindingAdapter(mData, mMapper);
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
}
