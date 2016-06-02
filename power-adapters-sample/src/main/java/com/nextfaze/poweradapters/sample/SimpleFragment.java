package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import butterknife.Bind;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;

public final class SimpleFragment extends BaseFragment {

    @NonNull
    private final NewsSimpleData mData = new NewsSimpleData();

    @NonNull
    private final PowerAdapter mAdapter = new DataBindingAdapter(mData, singletonMapper(new NewsItemBinder()));

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        mData.close();
        super.onDestroy();
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
