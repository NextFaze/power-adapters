package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.ErrorFormatter;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.DataBindingAdapter;
import com.nextfaze.poweradapters.DividerAdapter;
import com.nextfaze.poweradapters.HeaderAdapter;
import com.nextfaze.poweradapters.Mapper;
import com.nextfaze.poweradapters.PolymorphicMapper;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;

public final class SimpleFragment extends Fragment {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    private final NewsSimpleData mData = new NewsSimpleData(mNewsService);

    @NonNull
    private final Mapper mMapper = new PolymorphicMapper.Builder()
            .bind(NewsItem.class, new NewsItemBinder())
            .bind(NewsSection.class, new NewsSectionBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createSimpleAdapter(mData);

    @Bind(R.id.simple_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.simple_fragment_list)
    ListView mListView;

    @NonNull
    private PowerAdapter createSimpleAdapter(@NonNull Data<?> data) {
        PowerAdapter adapter = new DataBindingAdapter(data, mMapper);
        adapter = new HeaderAdapter.Builder(adapter)
                .headerResource(R.layout.news_header_item)
                .visibilityPolicy(HeaderAdapter.VisibilityPolicy.HIDE_IF_EMPTY)
                .build();
        adapter = new DividerAdapter.Builder(adapter)
                .innerItemResource(R.layout.divider_item)
                .outerItemResource(R.layout.divider_item)
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.simple_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mListView.setAdapter(toListAdapter(mAdapter));
        mDataLayout.setData(mData);
        mDataLayout.setErrorFormatter(new ErrorFormatter() {
            @Nullable
            @Override
            public String format(@NonNull Context context, @NonNull Throwable e) {
                return "Failed to load news: " + e.getMessage();
            }
        });
    }

    @Override
    public void onDestroyView() {
        // Nullify adapter to ensure ListView unregisters any observers, which will transitively disconnect all internal
        // observer registrations.
        mListView.setAdapter(null);
        mDataLayout.setData(null);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }
}
