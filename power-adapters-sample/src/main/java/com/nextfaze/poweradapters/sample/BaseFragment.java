package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nextfaze.asyncdata.widget.DataLayout;
import lombok.NonNull;

import static android.os.Looper.getMainLooper;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

abstract class BaseFragment extends Fragment {

    private static final int SCROLL_TO_END_DELAY = 50;

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @Bind(R.id.news_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list)
    ListView mListView;

    @Bind(R.id.news_recycler)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_data_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mDataLayout.setErrorFormatter(new NewsErrorFormatter());
        showCollectionView(CollectionView.LIST_VIEW);
    }

    @Override
    public void onDestroyView() {
        // IMPORTANT: RecyclerView requires we nullify the adapter once we're done with the view hierarchy.
        // Unlike ListView, it will NOT unregister its observers when detached from window. This causes leaks
        // if your adapter and/or data instance out-live the view hierarchy, which is a common occurrence.
        // Try commenting-out the following line to trigger LeakCanary for a demonstration of this scenario.
        mRecyclerView.setAdapter(null);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Reload").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onReloadClick();
                return true;
            }
        });
        menu.add("Refresh").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onRefreshClick();
                return true;
            }
        });
        menu.add("Invalidate").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onInvalidateClick();
                return true;
            }
        });
    }

    void scrollToEnd() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListView.smoothScrollToPosition(mListView.getCount() - 1);
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        }, SCROLL_TO_END_DELAY);
    }

    void showCollectionView(@NonNull CollectionView collectionView) {
        switch (collectionView) {
            case LIST_VIEW:
                mListView.setVisibility(VISIBLE);
                mRecyclerView.setVisibility(INVISIBLE);
                break;

            case RECYCLER_VIEW:
                mListView.setVisibility(INVISIBLE);
                mRecyclerView.setVisibility(VISIBLE);
                break;
        }
    }

    void showToast(@NonNull String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    SampleApplication getSampleApplication() {
        return (SampleApplication) getActivity().getApplication();
    }

    void onReloadClick() {
    }

    void onRefreshClick() {
    }

    void onInvalidateClick() {
    }

    enum CollectionView {
        LIST_VIEW, RECYCLER_VIEW
    }
}
