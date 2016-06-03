package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import butterknife.BindView;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.MapperBuilder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

import static com.nextfaze.poweradapters.sample.Utils.appendLoadNextButton;
import static com.nextfaze.poweradapters.sample.Utils.appendLoadingIndicator;

public final class ManualIncrementalFragment extends BaseFragment {

    @NonNull
    private final NewsIncrementalData mData = new NewsIncrementalData(100, 5);

    @NonNull
    private final Mapper mMapper = new MapperBuilder()
            .bind(NewsItem.class, new NewsItemBinder() {
                @Override
                void onNewsItemClick(@NonNull NewsItem newsItem, @NonNull View v) {
                    mData.remove(newsItem);
                }
            })
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createManualIncrementalAdapter(mData);

    @NonNull
    private PowerAdapter createManualIncrementalAdapter(@NonNull Data<?> data) {
        return new DataBindingAdapter(data, mMapper)
                .compose(appendLoadingIndicator(data))
                .compose(appendLoadNextButton(data, v -> onLoadNextClick()));
    }

    @BindView(R.id.data_layout)
    DataLayout mDataLayout;

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

    void onLoadNextClick() {
        mData.loadNext();
        scrollToEnd();
    }
}
