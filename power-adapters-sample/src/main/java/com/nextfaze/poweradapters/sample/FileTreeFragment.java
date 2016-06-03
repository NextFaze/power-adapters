package com.nextfaze.poweradapters.sample;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.TreeAdapter;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static android.graphics.Typeface.DEFAULT;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static com.google.common.base.Strings.repeat;
import static com.nextfaze.poweradapters.Condition.isTrue;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.data.DataConditions.isLoading;
import static com.nextfaze.poweradapters.sample.Utils.appendEmptyMessage;

public final class FileTreeFragment extends BaseFragment {

    private static final int MAX_DISPLAYED_FILES_PER_DIR = Integer.MAX_VALUE;

    @NonNull
    private final File mRootFile = new File("/");

    @Nullable
    private final Data<File> mRootData = new DirectoryData(mRootFile, MAX_DISPLAYED_FILES_PER_DIR);

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

        // Async adapter.
        PowerAdapter adapter = createFilesAdapter(mRootData, mRootFile, 0);
        setData(mRootData);

        // Simple, non-async adapter.
//        PowerAdapter adapter = createFilesAdapterSimple(mRootFile, 0, true);
//        mDataLayout.setData(ImmutableData.of(1));

        setAdapter(adapter);
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
    private PowerAdapter createFilesAdapterSimple(@NonNull final File file, final int depth, final boolean tree) {
        final AtomicReference<TreeAdapter> treeAdapterRef = new AtomicReference<>();
        Binder<File, TextView> binder = new AbstractBinder<File, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            public void bindView(@NonNull final File file, @NonNull TextView v, @NonNull final Holder holder) {
                v.setText(formatFile(file, depth));
                v.setTypeface(file.isDirectory() ? DEFAULT_BOLD : DEFAULT);
                v.setOnClickListener(v1 -> {
                    if (tree && file.isDirectory()) {
                        treeAdapterRef.get().toggleExpanded(holder.getPosition());
                    }
                });
            }
        };
        File[] filesArray = file.listFiles();
        final List<File> files = FluentIterable.from(
                filesArray != null ? Lists.newArrayList(filesArray) : Collections.<File>emptyList())
                .limit(MAX_DISPLAYED_FILES_PER_DIR)
                .toList();
        PowerAdapter adapter = new FileAdapter(files, singletonMapper(binder));
        treeAdapterRef.set(new TreeAdapter(adapter, position -> {
            File file1 = files.get(position);
            if (file1.isDirectory()) {
                return createFilesAdapterSimple(file1, depth + 1, true);
            }
            return PowerAdapter.EMPTY;
        }));
        if (tree) {
            adapter = treeAdapterRef.get();
        }

        return adapter;
    }

    @NonNull
    private PowerAdapter createFilesAdapter(@Nullable Data<File> overrideData,
                                            @NonNull final File file,
                                            final int depth) {
        final Data<File> data =
                overrideData != null ? overrideData : new DirectoryData(file, MAX_DISPLAYED_FILES_PER_DIR);
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
            File file1 = data.get(position);
            if (file1.isDirectory()) {
                return createFilesAdapter(null, file1, depth + 1);
            }
            return PowerAdapter.EMPTY;
        }));
        adapter = treeAdapterRef.get();

        if (mRootTreeAdapter == null) {
            mRootTreeAdapter = treeAdapterRef.get();
        }

        // Loading indicator
        adapter = adapter
                .append(asAdapter(R.layout.list_loading_item).showOnlyWhile(isTrue(depth != 0).and(isLoading(data))));

        // Empty message
        adapter = adapter.compose(appendEmptyMessage(data));

        return adapter;
    }

    @NonNull
    private ViewFactory directoryHeader(@NonNull final File file, final int depth) {
        return parent -> {
            TextView headerView = (TextView) LayoutInflater.from(getActivity())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            headerView.setBackgroundColor(0x20FFFFFF);
            headerView.setText(formatDir(file, depth));
            headerView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            return headerView;
        };
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

    @NonNull
    private static String formatDir(@NonNull File dir, int depth) {
        return repeat("    ", depth) + dir.getName() + ":";
    }
}
