package com.nextfaze.databind.sample;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.nextfaze.databind.ArrayData;
import com.nextfaze.databind.Data;
import com.nextfaze.databind.SimpleDataAdapter;
import com.nextfaze.databind.widget.DataLayout;
import lombok.NonNull;

import java.util.List;

public class NewsFragment extends Fragment {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    private final Data<NewsItem> mData = new ArrayData<NewsItem>() {
        @NonNull
        @Override
        protected List<? extends NewsItem> loadData() throws Exception {
            return mNewsService.getNews();
        }
    };

    @NonNull
    private final ListAdapter mAdapter = new SimpleDataAdapter<NewsItem>(mData, android.R.layout.simple_list_item_1) {
        @Override
        protected void bindView(@NonNull NewsItem newsItem, @NonNull View v, int position) {
            TextView textView = (TextView) v;
            textView.setText(newsItem.getTitle());
        }
    };

    @NonNull
    private DataLayout mDataLayout;

    @NonNull
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain Fragment instance to preserve data avoid reloading the data between config changes.
        setRetainInstance(true);
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
        mListView.setAdapter(mAdapter);
    }
}
