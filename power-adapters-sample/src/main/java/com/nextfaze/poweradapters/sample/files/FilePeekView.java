package com.nextfaze.poweradapters.sample.files;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.ViewHolder;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import com.nextfaze.poweradapters.data.widget.DataLayout;
import com.nextfaze.poweradapters.sample.R;

import java.util.List;
import java.util.Random;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static com.nextfaze.poweradapters.binding.ViewHolderBinder.create;
import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public final class FilePeekView extends RelativeLayout {

    private static final List<Integer> ICON_RESOURCES = ImmutableList.of(
            R.drawable.file_peek_view_icon_0,
            R.drawable.file_peek_view_icon_1,
            R.drawable.file_peek_view_icon_2
    );

    @NonNull
    private final Binder<File, View> mBinder = create(R.layout.file_peek_view_item, ItemViewHolder::new, (container, file, itemViewHolder, holder) -> {
        itemViewHolder.imageView.setImageResource(randomIconResource(file));
        itemViewHolder.titleView.setText(file.getName());
    });

    @BindView(R.id.data_layout)
    DataLayout mDataLayout;

    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    public FilePeekView(Context context) {
        this(context, null);
    }

    public FilePeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilePeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.file_peek_view, this);
        ButterKnife.bind(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, HORIZONTAL, false));
    }

    public void setFile(@NonNull File file) {
        DirectoryData data = new DirectoryData(file);
        mRecyclerView.setAdapter(toRecyclerAdapter(new DataBindingAdapter<>(mBinder, data)));
        mDataLayout.setData(data);
    }

    @DrawableRes
    private static int randomIconResource(@NonNull File file) {
        return ICON_RESOURCES.get(new Random(file.getName().hashCode()).nextInt(ICON_RESOURCES.size()));
    }

    static final class ItemViewHolder extends ViewHolder {

        @BindView(R.id.image)
        ImageView imageView;

        @BindView(R.id.title)
        TextView titleView;

        ItemViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
