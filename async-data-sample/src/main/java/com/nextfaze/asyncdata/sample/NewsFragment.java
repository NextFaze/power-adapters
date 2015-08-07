package com.nextfaze.asyncdata.sample;

import android.app.Fragment;
import android.content.Context;
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
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.nextfaze.asyncdata.ErrorFormatter;
import com.nextfaze.asyncdata.SimpleDataAdapter;
import com.nextfaze.asyncdata.widget.DataLayout;
import lombok.NonNull;

import javax.annotation.Nullable;

public final class NewsFragment extends Fragment {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    private final NewsSimpleData mSimpleData = new NewsSimpleData(mNewsService);

    @NonNull
    private final NewsIncrementalData mAutoIncrementalData = new NewsIncrementalData(mNewsService);

    @NonNull
    private final NewsIncrementalData mManualIncrementalData = new NewsIncrementalData(mNewsService);

    @NonNull
    private final ListAdapter mSimpleAdapter = new SimpleDataAdapter<NewsItem>(mSimpleData, android.R.layout.simple_list_item_1) {
        @Override
        protected void bindView(@NonNull final NewsItem newsItem, @NonNull View v, int position) {
            TextView textView = (TextView) v;
            textView.setText(newsItem.getTitle());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNewsItemClick(newsItem);
                }
            });
        }
    };


    @NonNull
    private final ListAdapter mAutoIncrementalAdapter = new SimpleDataAdapter<NewsItem>(mAutoIncrementalData, android.R.layout.simple_list_item_1) {
        @Override
        protected void bindView(@NonNull final NewsItem newsItem, @NonNull View v, int position) {
            TextView textView = (TextView) v;
            textView.setText(newsItem.getTitle());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNewsItemClick(newsItem);
                }
            });
        }
    };

    @NonNull
    private final ListAdapter mManualIncrementalAdapter = new SimpleDataAdapter<NewsItem>(mManualIncrementalData, android.R.layout.simple_list_item_1) {
        @Override
        protected void bindView(@NonNull final NewsItem newsItem, @NonNull View v, int position) {
            TextView textView = (TextView) v;
            textView.setText(newsItem.getTitle());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNewsItemClick(newsItem);
                }
            });
        }
    };

//    @NonNull
//    private final LoadNextAdapter mLoadNextAdapter = new LoadNextAdapter(mManualIncrementalData, mManualIncrementalAdapter, R.layout.load_next_item);

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
        // Start pre-loading as user approaches end of loaded content.
        mAutoIncrementalData.setLookAheadRowCount(10);
        // Never pre-load; let the user click 'load next' instead.
        mManualIncrementalData.setLookAheadRowCount(-1);
        // Reload data if hidden for a short time.
        mSimpleData.setAutoInvalidateDelay(3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Should dispose of data instances when finished with them.
        mSimpleData.close();
        mAutoIncrementalData.close();
        mManualIncrementalData.close();
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
        mDataLayout.setErrorFormatter(new ErrorFormatter() {
            @Nullable
            @Override
            public String format(@NonNull Context context, @NonNull Throwable e) {
                return "Failed to load news: " + e.getMessage();
            }
        });
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
        menu.add("Invalidate All").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onInvalidateAllClick();
                return true;
            }
        });
        menu.add("Deferred Invalidate Incremental").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onInvalidateIncrementalDeferredClick();
                return true;
            }
        });
    }

    @OnClick(R.id.news_fragment_button_simple)
    void onSimpleClick() {
        showSimple();
    }

    @OnClick(R.id.news_fragment_button_auto_incremental)
    void onAutoIncrementalClick() {
        showAutoIncremental();
    }

    @OnClick(R.id.news_fragment_button_manual_incremental)
    void onManualIncrementalClick() {
        showManualIncremental();
    }

    @OnClick(R.id.news_fragment_load_next)
    void onLoadNextClick() {
        mManualIncrementalData.loadNext();
    }

    void onClearAllClick() {
        mSimpleData.clear();
        mAutoIncrementalData.clear();
        mManualIncrementalData.clear();
    }

    void onInvalidateAllClick() {
        mSimpleData.invalidate();
        mAutoIncrementalData.invalidate();
        mManualIncrementalData.invalidate();
    }

    void onInvalidateIncrementalDeferredClick() {
        mAutoIncrementalData.invalidateDeferred();
        mManualIncrementalData.invalidateDeferred();
    }

    void onNewsItemClick(@NonNull NewsItem newsItem) {
        showToast("News item clicked: " + newsItem);
    }

    void showCheckedRadioButton(@IdRes int checkedId) {
        switch (checkedId) {
            case R.id.news_fragment_button_simple:
                showSimple();
                break;

            case R.id.news_fragment_button_auto_incremental:
                showAutoIncremental();
                break;

            case R.id.news_fragment_button_manual_incremental:
                showManualIncremental();
                break;
        }
    }

    void showSimple() {
        mDataLayout.setData(mSimpleData);
        mListView.setAdapter(mSimpleAdapter);
    }

    void showAutoIncremental() {
        mDataLayout.setData(mAutoIncrementalData);
        mListView.setAdapter(mAutoIncrementalAdapter);
    }

    void showManualIncremental() {
        mDataLayout.setData(mManualIncrementalData);
        mListView.setAdapter(mManualIncrementalAdapter);
    }

    void showToast(@NonNull String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
