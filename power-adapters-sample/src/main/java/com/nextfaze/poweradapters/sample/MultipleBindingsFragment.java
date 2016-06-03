package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import com.nextfaze.poweradapters.DividerAdapterBuilder;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.MapperBuilder;
import com.nextfaze.poweradapters.binding.ViewHolder;
import com.nextfaze.poweradapters.binding.ViewHolderBinder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.data.DataConditions.data;

public final class MultipleBindingsFragment extends BaseFragment {

    @NonNull
    private final NewsMultiTypeData mData = new NewsMultiTypeData();

    @NonNull
    private final Binder<NewsItem, View> mNewsItemBinder = new ViewHolderBinder<NewsItem, NewsItemHolder>(android.R.layout.simple_list_item_1) {
        @NonNull
        @Override
        protected NewsItemHolder newViewHolder(@NonNull View v) {
            return new NewsItemHolder(v);
        }

        @Override
        protected void bindViewHolder(@NonNull NewsItem newsItem,
                                      @NonNull NewsItemHolder newsItemHolder,
                                      @NonNull Holder holder) {
            newsItemHolder.labelView.setText(newsItem.getTitle());
        }
    };

    @NonNull
    private final Mapper mMapper = new MapperBuilder()
            .bind(NewsItem.class, mNewsItemBinder)
            .bind(NewsSection.class, new NewsSectionBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createSimpleAdapter(mData);

    @NonNull
    private PowerAdapter createSimpleAdapter(@NonNull Data<?> data) {
        return new DataBindingAdapter(data, mMapper)
                .prepend(asAdapter(R.layout.news_header_item).showOnlyWhile(data(data, d -> !d.isEmpty())))
                .compose(new DividerAdapterBuilder()
                        .innerResource(R.layout.list_divider_item)
                        .outerResource(R.layout.list_divider_item)
                        .emptyPolicy(DividerAdapterBuilder.EmptyPolicy.SHOW_LEADING));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Clear").setOnMenuItemClickListener(item -> {
            onClearClick();
            return true;
        });
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

    void onClearClick() {
        mData.clear();
    }

    static final class NewsItemHolder extends ViewHolder {

        @NonNull
        final TextView labelView;

        NewsItemHolder(@NonNull View view) {
            super(view);
            labelView = (TextView) view.findViewById(android.R.id.text1);
        }
    }
}
