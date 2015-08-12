package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import butterknife.Bind;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.DividerAdapter;
import com.nextfaze.poweradapters.HeaderAdapter;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.DataBindingAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;

public final class MultipleBindingsFragment extends BaseFragment {

    @NonNull
    private final NewsMultiTypeData mData = new NewsMultiTypeData();

    @NonNull
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder())
            .bind(NewsSection.class, new NewsSectionBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createSimpleAdapter(mData);

    @Bind(R.id.news_list_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list_fragment_list)
    ListView mListView;

    @NonNull
    private PowerAdapter createSimpleAdapter(@NonNull Data<?> data) {
        PowerAdapter adapter = new DataBindingAdapter(data, mMapper);
        adapter = new HeaderAdapter.Builder(adapter)
                .headerResource(R.layout.news_header_item)
                .visibilityPolicy(HeaderAdapter.VisibilityPolicy.HIDE_IF_EMPTY)
                .build();
        adapter = new DividerAdapter.Builder(adapter)
                .innerItemResource(R.layout.list_divider_item)
                .outerItemResource(R.layout.list_divider_item)
                .emptyPolicy(DividerAdapter.EmptyPolicy.SHOW_LEADING)
                .build();
        return adapter;
    }

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
    void onClearClick() {
        mData.clear();
    }

    @Override
    void onInvalidateClick() {
        mData.invalidate();
    }
}
