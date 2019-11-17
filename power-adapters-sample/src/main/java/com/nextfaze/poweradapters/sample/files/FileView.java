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

import static java.lang.Math.max;

public final class FileView extends RelativeLayout {

    ImageView mIconView;

    TextView mTitleView;

    TextView mSubtitleView;

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
        mIconView = findViewById(R.id.icon);
        mTitleView = findViewById(R.id.title);
        mSubtitleView = findViewById(R.id.subtitle);
        mPeekButton = findViewById(R.id.peek_button);
        mPeekButton.setOnClickListener(v -> onPeekClick());
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
