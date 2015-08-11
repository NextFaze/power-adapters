package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

abstract class BaseFragment extends Fragment {

    private static final int SCROLL_TO_END_DELAY = 50;

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @Bind(R.id.news_list_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_list_fragment_list)
    ListView mListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mDataLayout.setErrorFormatter(new NewsErrorFormatter());
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Clear").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onClearClick();
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
            }
        }, SCROLL_TO_END_DELAY);
    }

    void showToast(@NonNull String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    void onClearClick() {
    }

    void onInvalidateClick() {
    }
}
