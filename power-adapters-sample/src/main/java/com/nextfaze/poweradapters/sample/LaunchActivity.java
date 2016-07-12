package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.common.collect.ImmutableList;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.ListBindingAdapter;
import lombok.NonNull;

import java.util.List;

import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;

public final class LaunchActivity extends AppCompatActivity {

    @NonNull
    private final List<Sample> mSamples = ImmutableList.of(
            new Sample("Showcase", ShowcaseFragment.class),
            new Sample("Limit", LimitFragment.class),
            new Sample("Multiple Bindings", MultipleBindingsFragment.class),
            new Sample("Auto Incremental", AutoIncrementalFragment.class),
            new Sample("Manual Incremental", ManualIncrementalFragment.class),
            new Sample("Concatenation", ConcatFragment.class),
            new Sample("File Tree", FileTreeFragment.class)
    );

    @NonNull
    private final Binder<Sample, TextView> mSampleBinder = new AbstractBinder<Sample, TextView>(android.R.layout.simple_list_item_1) {
        @Override
        public void bindView(@NonNull final Sample sample, @NonNull TextView v, @NonNull Holder holder) {
            v.setText(sample.getName());
            v.setOnClickListener(v1 -> onSampleClick(sample));
        }
    };

    @NonNull
    private final PowerAdapter mAdapter = new ListBindingAdapter<>(singletonMapper(mSampleBinder), mSamples);

    @BindView(R.id.launch_activity_list)
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_activity);
        mListView.setAdapter(toListAdapter(mAdapter));
    }

    @Override
    protected void onDestroy() {
        mListView.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
    }

    private void onSampleClick(@NonNull Sample sample) {
        SampleActivity.start(this, sample);
    }
}
