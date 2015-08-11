package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import lombok.NonNull;

public final class SampleActivity extends AppCompatActivity {

    public static final String EXTRA_FRAGMENT_CLASS = "fragmentClass";

    public static void start(@NonNull Context context, @NonNull Class<? extends Fragment> fragmentClass) {
        context.startActivity(new Intent(context, SampleActivity.class)
                .putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity);
        if (savedInstanceState == null) {
            //noinspection unchecked
            Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) getIntent().getSerializableExtra(EXTRA_FRAGMENT_CLASS);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sample_activity_fragment, Fragment.instantiate(this, fragmentClass.getName()))
                    .commit();
        }
    }
}
