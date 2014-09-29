package com.nextfaze.databind.sample;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.nextfaze.databind.ArrayData;
import com.nextfaze.databind.Binder;
import com.nextfaze.databind.DataAdapterBuilder;
import com.nextfaze.databind.LoadingAdapter;
import com.nextfaze.databind.PagedArrayData;
import com.nextfaze.databind.TypedBinder;
import com.nextfaze.databind.widget.DataLayout;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    private final ArrayData<?> mData2 = new ArrayData<Object>() {
        @NonNull
        @Override
        protected List<Object> loadData() throws Exception {
            ArrayList<Object> data = new ArrayList<>();
            data.add(new NewsSection("Latest News"));
            data.addAll(mNewsService.getNews());
            data.add(new NewsSection("Yesterdays News"));
            data.addAll(mNewsService.getNews());
            return data;
        }
    };

    @NonNull
    private final PagedArrayData<?> mData = new PagedArrayData<Object>() {
        @NonNull
        @Override
        protected Page<Object> load(int offset, int count) throws Exception {
            ArrayList<Object> data = new ArrayList<>();
            data.addAll(mNewsService.getNews(offset, count));
            return new Page<Object>(data, offset, count, 50);
        }
    };

    @NonNull
    private final Binder mNewsItemBinder = new TypedBinder<NewsItem, TextView>(android.R.layout.simple_list_item_1) {
        @Override
        protected void bind(@NonNull NewsItem newsItem, @NonNull TextView textView, int position) {
            textView.setText(newsItem.getTitle());
        }
    };

    @NonNull
    private final Binder mNewsSectionBinder = new TypedBinder<NewsSection, TextView>(android.R.layout.simple_list_item_1, false) {
        @Override
        protected void bind(@NonNull NewsSection newsSection, @NonNull TextView textView, int position) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setText(newsSection.getTitle());
        }
    };

    @NonNull
    private final ListAdapter mAdapter = new DataAdapterBuilder(mData)
            .bind(NewsItem.class, mNewsItemBinder)
            .bind(NewsSection.class, mNewsSectionBinder)
            .build();

    @NonNull
    private final ListAdapter mLoadingAdapter = new LoadingAdapter(mData, mAdapter, R.layout.loading_item);

    @NonNull
    private DataLayout mDataLayout;

    @NonNull
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain Fragment instance to preserve data avoid reloading the data between config changes.
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mData.close();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDataLayout = (DataLayout) view.findViewById(R.id.data_layout);
        mListView = (ListView) view.findViewById(R.id.list);
        mDataLayout.setData(mData);
        mListView.setAdapter(mLoadingAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Clear").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mData.clear();
                return true;
            }
        });
    }
}
