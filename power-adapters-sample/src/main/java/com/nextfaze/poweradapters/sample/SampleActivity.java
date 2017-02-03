package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public final class SampleActivity extends AppCompatActivity {

    public static final String EXTRA_SAMPLE = "sample";

    public static void start(@NonNull Context context, @NonNull Sample sample) {
        context.startActivity(new Intent(context, SampleActivity.class)
                .putExtra(EXTRA_SAMPLE, sample));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Sample sample = getIntent().getParcelableExtra(EXTRA_SAMPLE);
        setContentView(R.layout.sample_activity);
        setTitle(sample.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sample_activity_fragment, Fragment.instantiate(this, sample.getFragmentClass().getName()))
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
