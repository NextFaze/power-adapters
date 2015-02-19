package com.nextfaze.databind.sample;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.nextfaze.databind.ArrayData;
import com.nextfaze.databind.Binder;
import com.nextfaze.databind.DataAdapterBuilder;
import com.nextfaze.databind.ErrorFormatter;
import com.nextfaze.databind.IncrementalArrayData;
import com.nextfaze.databind.LoadingAdapter;
import com.nextfaze.databind.TypedBinder;
import com.nextfaze.databind.widget.DataLayout;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class NewsFragment extends Fragment {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    private final ArrayData<?> mSimpleData = new ArrayData<Object>() {
        @NonNull
        @Override
        protected List<Object> loadData() throws Exception {
            ArrayList<Object> data = new ArrayList<>();
            data.add(new NewsSection("Latest News"));
            data.addAll(mNewsService.getNewsFlaky());
            data.add(new NewsSection("Yesterdays News"));
            data.addAll(mNewsService.getNewsFlaky());
            return data;
        }
    };

    @NonNull
    private final IncrementalArrayData<?> mIncrementalData = new IncrementalArrayData<Object>() {

        private static final int TOTAL = 50;
        private static final int INCREMENT = 10;

        private volatile int mOffset;

        @Nullable
        @Override
        protected List<?> load() throws Throwable {
            int offset = mOffset;
            if (offset >= TOTAL) {
                return null;
            }
            mOffset = offset + INCREMENT;
            return mNewsService.getNews(offset, INCREMENT);
        }

        @Override
        protected void onInvalidate() {
            mOffset = 0;
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
    private final ListAdapter mSimpleAdapter = new DataAdapterBuilder(mSimpleData)
            .bind(NewsItem.class, mNewsItemBinder)
            .build();

    @NonNull
    private final ListAdapter mIncrementalAdapter = LoadingAdapter.create(
            mIncrementalData,
            new DataAdapterBuilder(mIncrementalData)
                    .bind(NewsItem.class, mNewsItemBinder)
                    .bind(NewsSection.class, mNewsSectionBinder)
                    .build(),
            R.layout.loading_item
    );

    @NonNull
    private final ErrorFormatter mErrorFormatter = new ErrorFormatter() {
        @Nullable
        @Override
        public String format(@NonNull Context context, @NonNull Throwable e) {
            return "Error: " + e.getMessage();
        }
    };

    @InjectView(R.id.news_fragment_radio_group)
    RadioGroup mRadioGroup;

    @InjectView(R.id.news_fragment_data_layout)
    DataLayout mDataLayout;

    @InjectView(R.id.news_fragment_list)
    ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain Fragment instance to preserve data avoid reloading the data between config changes.
        setRetainInstance(true);
        setHasOptionsMenu(true);
        // Start pre-loading sooner.
        mIncrementalData.setLookAheadRowCount(10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Must dispose of data instances when finished with them.
        mSimpleData.close();
        mIncrementalData.close();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mDataLayout.setErrorFormatter(mErrorFormatter);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        showCheckedRadioButton(mRadioGroup.getCheckedRadioButtonId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Clear All").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onClearAllClick();
                return true;
            }
        });
    }

    @OnClick(R.id.news_fragment_button_simple)
    void onSimpleClick() {
        showSimple();
    }

    @OnClick(R.id.news_fragment_button_incremental)
    void onIncrementalClick() {
        showIncremental();
    }

    void onClearAllClick() {
        mSimpleData.clear();
        mIncrementalData.clear();
    }

    private void showCheckedRadioButton(@IdRes int checkedId) {
        switch (checkedId) {
            case R.id.news_fragment_button_simple:
                showSimple();
                break;

            case R.id.news_fragment_button_incremental:
                showIncremental();
                break;
        }
    }

    void showSimple() {
        mDataLayout.setData(mSimpleData);
        mListView.setAdapter(mSimpleAdapter);
    }

    void showIncremental() {
        mDataLayout.setData(mIncrementalData);
        mListView.setAdapter(mIncrementalAdapter);
    }
}
