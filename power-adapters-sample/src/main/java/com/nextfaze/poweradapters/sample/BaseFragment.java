package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
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
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.Predicate;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

import static android.os.Looper.getMainLooper;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.nextfaze.poweradapters.Condition.not;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.data.DataConditions.*;

abstract class BaseFragment extends Fragment {

    private static final int SCROLL_TO_END_DELAY = 50;
    private static final int ANIMATION_DURATION = 150;

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.list)
    ListView mListView;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

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
        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(ANIMATION_DURATION);
        animator.setRemoveDuration(ANIMATION_DURATION);
        animator.setChangeDuration(ANIMATION_DURATION);
        animator.setMoveDuration(ANIMATION_DURATION);
        mRecyclerView.setItemAnimator(animator);
        mDataLayout.setErrorFormatter(new SimpleErrorFormatter());
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

    @NonNull
    PowerAdapter.Transformer appendLoadingIndicator(@NonNull final Data<?> data) {
        return new PowerAdapter.Transformer() {
            @NonNull
            @Override
            public PowerAdapter transform(@NonNull PowerAdapter adapter) {
                return adapter.append(asAdapter(R.layout.list_loading_item).showOnlyWhile(data(data, new Predicate<Data<?>>() {
                    @Override
                    public boolean apply(Data<?> data) {
                        return data.isLoading() && !data.isEmpty();
                    }
                })));
            }
        };
    }

    @NonNull
    PowerAdapter.Transformer appendEmptyMessage(@NonNull final Data<?> data, @LayoutRes final int layoutResource) {
        return new PowerAdapter.Transformer() {
            @NonNull
            @Override
            public PowerAdapter transform(@NonNull PowerAdapter adapter) {
                return adapter.append(asAdapter(layoutResource).showOnlyWhile(isEmpty(data).and(not(isLoading(data)))));
            }
        };
    }

    enum CollectionView {
        LIST_VIEW, RECYCLER_VIEW
    }
}
