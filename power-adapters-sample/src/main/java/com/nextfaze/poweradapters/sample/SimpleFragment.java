package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import butterknife.Bind;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.asyncdata.DataBindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;

public final class SimpleFragment extends BaseFragment {

    @NonNull
    private final NewsSimpleData mData = new NewsSimpleData();

    @NonNull
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = new DataBindingAdapter(mData, mMapper);

    @Bind(R.id.news_list_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list_fragment_list)
    ListView mListView;

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
