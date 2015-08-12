package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.common.collect.ImmutableList;
import com.nextfaze.poweradapters.Binder;
import com.nextfaze.poweradapters.BindingAdapter;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.Mapper;
import com.nextfaze.poweradapters.PolymorphicMapperBuilder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.TypedBinder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.List;

import static com.nextfaze.poweradapters.PowerAdapters.toListAdapter;

public final class LaunchActivity extends AppCompatActivity {

    @NonNull
    private final List<? extends Sample> mSamples = ImmutableList.of(
            new Sample("Simple", SimpleFragment.class),
            new Sample("Multiple Bindings", MultipleBindingsFragment.class),
            new Sample("Auto Incremental", AutoIncrementalFragment.class),
            new Sample("Manual Incremental", ManualIncrementalFragment.class),
            new Sample("Long-Lived Data", LongLivedDataFragment.class)
    );

    @NonNull
    private final Binder mSampleBinder = new TypedBinder<Sample, TextView>(android.R.layout.simple_list_item_1) {
        @Override
        protected void bind(@NonNull final Sample sample, @NonNull TextView v, @NonNull Holder holder) {
            v.setText(sample.getName());
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSampleClick(sample);
                }
            });
        }
    };

    @NonNull
    private final Mapper mMapper = new PolymorphicMapperBuilder()
            .bind(Sample.class, mSampleBinder)
            .build();

    @NonNull
    private final PowerAdapter mAdapter = new BindingAdapter(mMapper) {
        @Override
        public int getItemCount() {
            return mSamples.size();
        }

        @NonNull
        @Override
        protected Object getItem(int position) {
            return mSamples.get(position);
        }
    };

    @Bind(R.id.launch_activity_list)
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
        SampleActivity.start(this, sample.getFragmentClass());
    }

    @Getter
    @Accessors(prefix = "m")
    private final static class Sample {

        @NonNull
        private final String mName;

        @NonNull
        private final Class<? extends Fragment> mFragmentClass;

        Sample(@NonNull String name, @NonNull Class<? extends Fragment> fragmentClass) {
            mName = name;
            mFragmentClass = fragmentClass;
        }
    }
}
