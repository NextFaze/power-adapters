package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import com.nextfaze.poweradapters.DividerAdapterBuilder;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.PolymorphicMapperBuilder;
import com.nextfaze.poweradapters.binding.ViewHolder;
import com.nextfaze.poweradapters.binding.ViewHolderBinder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

import static com.nextfaze.poweradapters.Condition.not;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;
import static com.nextfaze.poweradapters.data.DataConditions.isEmpty;

public final class MultipleBindingsFragment extends BaseFragment {

    @NonNull
    private final NewsMultiTypeData mData = new NewsMultiTypeData();

    @NonNull
    private final Binder mNewsItemBinder = new ViewHolderBinder<NewsItem, NewsItemHolder>(android.R.layout.simple_list_item_1) {
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
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(NewsItem.class, mNewsItemBinder)
            .bind(NewsSection.class, new NewsSectionBinder())
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createSimpleAdapter(mData);

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.list)
    ListView mListView;

    @NonNull
    private PowerAdapter createSimpleAdapter(@NonNull Data<?> data) {
        return new DataBindingAdapter(data, mMapper)
                .prepend(asAdapter(R.layout.news_header_item).showOnlyWhile(not(isEmpty(data))))
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
        mListView.setAdapter(toListAdapter(mAdapter));
        mDataLayout.setData(mData);
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
        showToast("Data invalidated; background the app or change orientation to trigger reload");
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
