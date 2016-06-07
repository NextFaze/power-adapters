package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.TreeAdapter;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;

import static android.graphics.Typeface.DEFAULT;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static com.google.common.base.Strings.repeat;
import static com.nextfaze.poweradapters.Condition.isTrue;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.data.DataConditions.isLoading;
import static com.nextfaze.poweradapters.sample.Utils.emptyMessage;

public final class FileTreeFragment extends BaseFragment {

    @NonNull
    private final File mRootFile = File.rootFile();

    @NonNull
    private final Data<File> mRootData = new DirectoryData(mRootFile);

    @Nullable
    private TreeAdapter mRootTreeAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter(createFilesAdapter(mRootData, mRootFile, 0));
        setData(mRootData);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Expand All").setOnMenuItemClickListener(item -> {
            expandAll();
            return true;
        });
        menu.add("Collapse All").setOnMenuItemClickListener(item -> {
            collapseAll();
            return true;
        });
    }

    @NonNull
    private PowerAdapter createFilesAdapter(@Nullable Data<File> overrideData,
                                            @NonNull final File file,
                                            final int depth) {
        final Data<File> data =
                overrideData != null ? overrideData : new DirectoryData(file);
        final AtomicReference<TreeAdapter> treeAdapterRef = new AtomicReference<>();

        Binder<File, TextView> binder = new AbstractBinder<File, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            public void bindView(@NonNull File file, @NonNull TextView v, @NonNull final Holder holder) {
                v.setText(formatFile(file, depth));
                v.setTypeface(file.isDirectory() ? DEFAULT_BOLD : DEFAULT);
                if (file.isDirectory()) {
                    v.setOnClickListener(v1 -> {
                        TreeAdapter treeAdapter = treeAdapterRef.get();
                        int position = holder.getPosition();
                        if (treeAdapter.isExpanded(position)) {
                            treeAdapter.setExpanded(position, false);
                        } else {
                            treeAdapter.setExpanded(position, true);
                        }
                    });
                }
            }
        };

        PowerAdapter adapter = new DataBindingAdapter(data, singletonMapper(binder));
        treeAdapterRef.set(new TreeAdapter(adapter, position -> {
            File f = data.get(position);
            if (f.isDirectory()) {
                return createFilesAdapter(null, f, depth + 1);
            }
            return PowerAdapter.EMPTY;
        }));
        adapter = treeAdapterRef.get();

        if (mRootTreeAdapter == null) {
            mRootTreeAdapter = treeAdapterRef.get();
        }

        // Loading indicator
        adapter = adapter.append(loadingIndicator(data, depth));

        // Empty message
        adapter = adapter.append(emptyMessage(data));

        return adapter;
    }

    @NonNull
    private static PowerAdapter loadingIndicator(@NonNull Data<File> data, int depth) {
        return asAdapter(R.layout.list_loading_item).showOnlyWhile(isTrue(depth != 0).and(isLoading(data)));
    }

    private void expandAll() {
        if (mRootTreeAdapter != null) {
            mRootTreeAdapter.setAllExpanded(true);
        }
    }

    private void collapseAll() {
        if (mRootTreeAdapter != null) {
            mRootTreeAdapter.setAllExpanded(false);
        }
    }

    @NonNull
    private static String formatFile(@NonNull File file, int depth) {
        return repeat("    ", depth) + (file.isDirectory() ? file.getName() + "/" : file.getName());
    }
}
