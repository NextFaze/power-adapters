package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.nextfaze.poweradapters.PowerAdapter;

import static com.nextfaze.poweradapters.sample.News.createNewsAdapter;
import static com.nextfaze.poweradapters.sample.Utils.loadingIndicatorWhileNonEmpty;

public final class AutoIncrementalFragment extends BaseFragment {

    @NonNull
    private final NewsData mData = new NewsData(50, 10);

    @NonNull
    private final PowerAdapter mAdapter = createNewsAdapter(mData).append(loadingIndicatorWhileNonEmpty(mData));

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
        setAdapter(mAdapter);
        setData(mData);
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
    }
}
