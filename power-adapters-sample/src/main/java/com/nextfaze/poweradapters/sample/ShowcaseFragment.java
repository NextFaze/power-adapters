package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.data.Data;

import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.PowerAdapter.concat;

public final class ShowcaseFragment extends BaseFragment {

    @NonNull
    private CatData mCatData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mCatData = Cats.createData(getContext());
    }

    @Override
    public void onDestroy() {
        mCatData.close();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        File rootFile = File.rootDir();
        Data<File> rootData = new DirectoryData(rootFile);
        PowerAdapter adapter = concat(
                header("Files"),
                FileTree.createAdapter(rootData, rootFile),
                header("Cats"),
                Cats.createAdapter(mCatData)
        );
        setAdapter(adapter);
        setDatas(rootData, mCatData);
    }

    @NonNull
    private static PowerAdapter header(@NonNull String title) {
        return asAdapter(parent -> {
            TextView v = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item, parent, false);
            v.setText(title);
            return v;
        });
    }
}
