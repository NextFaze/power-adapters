package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.sample.Utils.*;

public final class ManualIncrementalFragment extends BaseFragment {

    @NonNull
    private final NewsData mData = new NewsData(100, 5);

    @NonNull
    private final PowerAdapter mAdapter = createNewsAdapter(mData)
            .append(loadingIndicator(mData))
            .append(loadNextButton(mData, v -> onLoadNextClick()));

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

    void onLoadNextClick() {
        mData.loadNext();
        scrollToEnd();
    }
}
