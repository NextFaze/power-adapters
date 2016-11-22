package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.DividerAdapterBuilder;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.BinderWrapper;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.MapperBuilder;
import com.nextfaze.poweradapters.binding.ViewHolder;
import com.nextfaze.poweradapters.binding.ViewHolderBinder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.data.DataConditions.data;
import static com.nextfaze.poweradapters.sample.NewsItem.Type.POLITICS;
import static java.util.Collections.singleton;

public final class MultipleBindingsFragment extends BaseFragment {

    @NonNull
    private final NewsMultiTypeData mData = new NewsMultiTypeData();

    @NonNull
    private final Binder<NewsItem, NewsItemView> mPoliticsNewsItemBinder = new BinderWrapper<NewsItem, NewsItemView>(new NewsItemBinder(mData.asList())) {
        @Override
        public void bindView(@NonNull Container container, @NonNull NewsItem newsItem, @NonNull NewsItemView v, @NonNull Holder holder) {
            super.bindView(container, newsItem, v, holder);
            v.setTags(singleton("Boring!"));
        }
    };

    @NonNull
    private final Binder<BlogPost, View> mBlogPostBinder = new ViewHolderBinder<BlogPost, BlogPostHolder>(android.R.layout.simple_list_item_1) {
        @NonNull
        @Override
        protected BlogPostHolder newViewHolder(@NonNull View v) {
            return new BlogPostHolder(v);
        }

        @Override
        protected void bindViewHolder(@NonNull Container container,
                                      @NonNull BlogPost blogPost,
                                      @NonNull BlogPostHolder blogPostHolder,
                                      @NonNull Holder holder) {
            blogPostHolder.labelView.setText("Blog: " + blogPost.getTitle());
        }
    };

    @NonNull
    private final Mapper mMapper = new MapperBuilder()
            .bind(NewsSection.class, new NewsSectionBinder())
            .bind(NewsItem.class, mPoliticsNewsItemBinder, newsItem -> newsItem.getType() == POLITICS)
            .bind(NewsItem.class, new NewsItemBinder(mData.asList()))
            .bind(BlogPost.class, mBlogPostBinder)
            .build();

    @NonNull
    private final PowerAdapter mAdapter = createSimpleAdapter(mData);

    @NonNull
    private PowerAdapter createSimpleAdapter(@NonNull Data<?> data) {
        return new DataBindingAdapter<>(data, mMapper)
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

    private static final class BlogPostHolder extends ViewHolder {

        @NonNull
        final TextView labelView;

        BlogPostHolder(@NonNull View view) {
            super(view);
            labelView = (TextView) view.findViewById(android.R.id.text1);
        }
    }
}
