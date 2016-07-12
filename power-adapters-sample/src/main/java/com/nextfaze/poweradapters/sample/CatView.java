package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import lombok.NonNull;

public final class CatView extends CardView {

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.title)
    TextView mTitleView;

    @BindView(R.id.subtitle)
    TextView mSubtitleView;

    public CatView(Context context) {
        this(context, null);
    }

    public CatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUseCompatPadding(true);
        inflate(context, R.layout.cat_view, this);
        ButterKnife.bind(this);
    }

    public void setCat(@NonNull Cat cat) {
        // Image
        Picasso.with(getContext())
                .load(cat.getImageUri())
                .into(mImageView);

        // Title
        mTitleView.setText(cat.getName());

        // Subtitle
        mSubtitleView.setText(cat.getCountry());
    }
}
