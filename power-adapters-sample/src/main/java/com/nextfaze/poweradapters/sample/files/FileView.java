package com.nextfaze.poweradapters.sample.files;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nextfaze.poweradapters.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.lang.Math.max;

public final class FileView extends RelativeLayout {

    @BindView(R.id.icon)
    ImageView mIconView;

    @BindView(R.id.title)
    TextView mTitleView;

    @BindView(R.id.subtitle)
    TextView mSubtitleView;

    @BindView(R.id.peek_button)
    View mPeekButton;

    @Nullable
    private OnPeekListener mOnPeekListener;

    public FileView(Context context) {
        this(context, null);
    }

    public FileView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.file_view, this);
        ButterKnife.bind(this);
        updatePeekButton();
    }

    public void setFile(@NonNull File file) {
        mTitleView.setText(file.getName());
        if (file.isDirectory()) {
            mIconView.setImageResource(R.drawable.file_icon_dir);
            mSubtitleView.setText(file.getCount() + " items");
        } else {
            mIconView.setImageResource(R.drawable.file_icon_file);
            mSubtitleView.setText(file.getSize() + " mb");
        }
    }

    public void setDepth(int depth) {
        depth = max(0, depth);
        setPadding(depth * getResources().getDimensionPixelSize(R.dimen.file_view_padding_increment), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    public void setOnPeekListener(@Nullable OnPeekListener onPeekListener) {
        if (mOnPeekListener != onPeekListener) {
            mOnPeekListener = onPeekListener;
            updatePeekButton();
        }
    }

    @OnClick(R.id.peek_button)
    void onPeekClick() {
        if (mOnPeekListener != null) {
            mOnPeekListener.onPeek();
        }
    }

    private void updatePeekButton() {
        mPeekButton.setVisibility(mOnPeekListener != null ? VISIBLE : GONE);
    }

    public interface OnPeekListener {
        void onPeek();
    }
}
