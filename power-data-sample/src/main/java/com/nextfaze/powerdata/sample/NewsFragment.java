package com.nextfaze.powerdata.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.nextfaze.powerdata.SimpleDataAdapter;
import com.nextfaze.powerdata.widget.DataLayout;
import com.nextfaze.powerdata.widget.ErrorFormatter;
import lombok.NonNull;

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

    @Bind(R.id.news_fragment_radio_group)
    RadioGroup mRadioGroup;

    @Bind(R.id.news_fragment_data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.news_fragment_list)
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
        ButterKnife.bind(this, view);
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
        ButterKnife.unbind(this);
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

    void onReloadClick() {
        mSimpleData.reload();
        mAutoIncrementalData.reload();
        mManualIncrementalData.reload();
    }

    void onRefreshClick() {
        mSimpleData.refresh();
        mAutoIncrementalData.refresh();
        mManualIncrementalData.refresh();
    }

    void onInvalidateClick() {
        mSimpleData.invalidate();
        mAutoIncrementalData.invalidate();
        mManualIncrementalData.invalidate();
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
