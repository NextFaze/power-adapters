package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Iterator;
import java.util.Set;

import static java.lang.String.format;

@Accessors(prefix = "m")
public final class NewsItemView extends RelativeLayout {

    @BindView(R.id.title)
    TextView mTitleView;

    @BindView(R.id.tags)
    TextView mTagsView;

    @BindView(R.id.multiple_check_box)
    CheckBox mMultipleCheckBox;

    @Setter
    @Nullable
    private OnClickListener mRemoveOnClickListener;

    @Setter
    @Nullable
    private OnClickListener mInsertBeforeOnClickListener;

    @Setter
    @Nullable
    private OnClickListener mInsertAfterOnClickListener;

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
        if (mRemoveOnClickListener != null) {
            mRemoveOnClickListener.onClick(this);
        }
    }

    @OnClick(R.id.insert_before_button)
    void onInsertBeforeClick() {
        if (mInsertBeforeOnClickListener != null) {
            mInsertBeforeOnClickListener.onClick(this);
        }
    }

    @OnClick(R.id.insert_after_button)
    void onInsertAfterClick() {
        if (mInsertAfterOnClickListener != null) {
            mInsertAfterOnClickListener.onClick(this);
        }
    }

    public boolean isMultipleChecked() {
        return mMultipleCheckBox.isChecked();
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
}
