package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Iterator;
import java.util.Set;

import static java.lang.String.format;

@Accessors(prefix = "m")
public final class NewsItemView extends RelativeLayout {

    private static final int CLICK_COUNT = 1;
    private static final int LONG_CLICK_COUNT = 5;

    @BindView(R.id.title)
    TextView mTitleView;

    @BindView(R.id.tags)
    TextView mTagsView;

    @Setter
    @Nullable
    private OnRemoveListener mOnRemoveListener;

    @Setter
    @Nullable
    private OnInsertListener mOnInsertBeforeListener;

    @Setter
    @Nullable
    private OnInsertListener mOnInsertAfterListener;

    public NewsItemView(Context context) {
        this(context, null);
    }

    public NewsItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewsItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.news_item, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.remove_button)
    void onRemoveClick() {
        if (mOnRemoveListener != null) {
            mOnRemoveListener.onRemove(CLICK_COUNT);
        }
    }

    @OnLongClick(R.id.remove_button)
    boolean onRemoveLongClick() {
        if (mOnRemoveListener != null) {
            mOnRemoveListener.onRemove(LONG_CLICK_COUNT);
        }
        return true;
    }

    @OnClick(R.id.insert_before_button)
    void onInsertBeforeClick() {
        if (mOnInsertBeforeListener != null) {
            mOnInsertBeforeListener.onInsert(CLICK_COUNT);
        }
    }

    @OnLongClick(R.id.insert_before_button)
    boolean onInsertBeforeLongClick() {
        if (mOnInsertBeforeListener != null) {
            mOnInsertBeforeListener.onInsert(LONG_CLICK_COUNT);
        }
        return true;
    }

    @OnClick(R.id.insert_after_button)
    void onInsertAfterClick() {
        if (mOnInsertAfterListener != null) {
            mOnInsertAfterListener.onInsert(CLICK_COUNT);
        }
    }

    @OnLongClick(R.id.insert_after_button)
    boolean onInsertAfterLongClick() {
        if (mOnInsertAfterListener != null) {
            mOnInsertAfterListener.onInsert(LONG_CLICK_COUNT);
        }
        return true;
    }

    public void setNewsItem(@NonNull NewsItem newsItem) {
        mTitleView.setText(format("%s (%s)", newsItem.getTitle(), newsItem.getType().toString().toLowerCase()));
    }

    public void setTags(@NonNull Set<String> tags) {
        CharSequence formatted = formatTags(tags);
        mTagsView.setText(formatted);
        mTagsView.setVisibility(formatted != null ? VISIBLE : GONE);
    }

    @Nullable
    private CharSequence formatTags(@NonNull Set<String> tags) {
        if (tags.isEmpty()) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for (Iterator<String> it = tags.iterator(); it.hasNext(); ) {
            String tag = it.next();
            b.append(tag);
            if (it.hasNext()) {
                b.append(", ");
            }
        }
        return b;
    }

    public interface OnInsertListener {
        void onInsert(int count);
    }

    public interface OnRemoveListener {
        void onRemove(int count);
    }
}
