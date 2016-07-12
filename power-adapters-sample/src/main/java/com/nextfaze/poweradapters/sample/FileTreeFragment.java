package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import com.nextfaze.poweradapters.data.Data;

public final class FileTreeFragment extends BaseFragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        File rootFile = File.rootDir();
        Data<File> rootData = new DirectoryData(rootFile);
        setAdapter(FileTree.createAdapter(rootData, rootFile));
        setData(rootData);
    }
}
