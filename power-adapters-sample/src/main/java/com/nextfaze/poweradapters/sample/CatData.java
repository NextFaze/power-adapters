package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.nextfaze.poweradapters.data.IncrementalArrayData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import static com.google.common.io.CharStreams.readLines;
import static java.lang.Math.max;
import static java.nio.charset.Charset.defaultCharset;

class CatData extends IncrementalArrayData<Cat> {

    private static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults(CharMatcher.anyOf("\""));

    @NonNull
    private final Context mContext;

    private volatile int mOffset;

    CatData(@NonNull Context context) {
        mContext = context.getApplicationContext();
        setLookAheadRowCount(-1);
    }

    @Nullable
    @Override
    protected Result<? extends Cat> load() throws Throwable {
        Thread.sleep(1500);
        List<Cat> cats = loadFromCsv(mContext);
        int count = 3;
        Result<Cat> result = new Result<>(cats.subList(mOffset, mOffset + count), max(0,
                cats.size() - mOffset - count));
        mOffset += count;
        return result;
    }

    @Override
    protected void onLoadBegin() {
        mOffset = 0;
    }

    @NonNull
    private static List<Cat> loadFromCsv(@NonNull Context context) throws IOException {
        // CSV columns: breed, country, origin, body type, coat, pattern
        Reader reader = new InputStreamReader(context.getAssets().open("cats.csv"), defaultCharset());
        return FluentIterable.from(readLines(reader))
                .transform(line -> SPLITTER.splitToList(line))
                .transform(values -> Cat.create(values.get(0), values.get(1)))
                .toList();
    }
}
