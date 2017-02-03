package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.widget.DataLayout;

import static android.os.Looper.getMainLooper;
import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

abstract class BaseFragment extends Fragment {

    private static final int SCROLL_TO_END_DELAY = 50;
    private static final int ANIMATION_DURATION = 150;

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @BindView(R.id.data_layout)
    DataLayout mDataLayout;

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.data_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(ANIMATION_DURATION);
        animator.setRemoveDuration(ANIMATION_DURATION);
        animator.setChangeDuration(ANIMATION_DURATION);
        animator.setMoveDuration(ANIMATION_DURATION);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(animator);
        mDataLayout.setErrorFormatter(new SimpleErrorFormatter());
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Reload").setOnMenuItemClickListener(item -> {
            onReloadClick();
            return true;
        });
        menu.add("Refresh").setOnMenuItemClickListener(item -> {
            onRefreshClick();
            return true;
        });
        menu.add("Invalidate").setOnMenuItemClickListener(item -> {
            onInvalidateClick();
            return true;
        });
    }

    void setAdapter(@NonNull PowerAdapter adapter) {
        mRecyclerView.setAdapter(toRecyclerAdapter(adapter));
    }

    void setData(@NonNull Data<?> data) {
        mDataLayout.setData(data);
    }

    void setDatas(@NonNull Data<?>... datas) {
        mDataLayout.setDatas(datas);
    }

    void scrollToEnd() {
        mHandler.postDelayed(() ->
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1), SCROLL_TO_END_DELAY);
    }

    void showToast(@NonNull String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    void onReloadClick() {
    }

    void onRefreshClick() {
    }

    void onInvalidateClick() {
    }
}
