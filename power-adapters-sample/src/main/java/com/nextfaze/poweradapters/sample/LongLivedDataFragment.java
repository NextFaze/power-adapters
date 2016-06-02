package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import butterknife.BindView;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.MapperBuilder;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

public final class LongLivedDataFragment extends BaseFragment {

    @NonNull
    private final Mapper mMapper = new MapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder())
            .build();

    private NewsSimpleData mData;

    private PowerAdapter mAdapter;

    @BindView(R.id.data_layout)
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
        setAdapter(mAdapter);
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
