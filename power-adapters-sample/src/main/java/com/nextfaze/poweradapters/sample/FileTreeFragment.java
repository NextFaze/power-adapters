package com.nextfaze.poweradapters.sample;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
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
import com.nextfaze.poweradapters.data.widget.DataLayout;
import lombok.NonNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static android.graphics.Typeface.DEFAULT;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.google.common.base.Strings.repeat;
import static com.nextfaze.poweradapters.Condition.isTrue;
import static com.nextfaze.poweradapters.PowerAdapter.asAdapter;
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.data.DataConditions.isLoading;
import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public class FileTreeFragment extends BaseFragment {

    private static final int MAX_DISPLAYED_FILES_PER_DIR = Integer.MAX_VALUE;

    @NonNull
    private final File mRootFile = new File("/");

    @Nullable
    private final Data<File> mRootData = new DirectoryData(mRootFile, MAX_DISPLAYED_FILES_PER_DIR);

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

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
        mDataLayout.setData(mRootData);

        // Simple, non-async adapter.
//        PowerAdapter adapter = createFilesAdapterSimple(mRootFile, 0, true);
//        mDataLayout.setData(ImmutableData.of(1));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), VERTICAL, false));
        mRecyclerView.setAdapter(toRecyclerAdapter(adapter));
        showCollectionView(CollectionView.RECYCLER_VIEW);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Expand All").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                expandAll();
                return true;
            }
        });
        menu.add("Collapse All").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                collapseAll();
                return true;
            }
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
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tree && file.isDirectory()) {
                            treeAdapterRef.get().toggleExpanded(holder.getPosition());
                        }
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
        treeAdapterRef.set(new TreeAdapter(adapter, new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                File file = files.get(position);
                if (file.isDirectory()) {
                    return createFilesAdapterSimple(file, depth + 1, true);
                }
                return PowerAdapter.EMPTY;
            }
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
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TreeAdapter treeAdapter = treeAdapterRef.get();
                            int position = holder.getPosition();
                            if (treeAdapter.isExpanded(position)) {
                                treeAdapter.setExpanded(position, false);
                            } else {
                                treeAdapter.setExpanded(position, true);
                            }
                        }
                    });
                }
            }
        };

        PowerAdapter adapter = new DataBindingAdapter(data, singletonMapper(binder));
        treeAdapterRef.set(new TreeAdapter(adapter, new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                File file = data.get(position);
                if (file.isDirectory()) {
                    return createFilesAdapter(null, file, depth + 1);
                }
                return PowerAdapter.EMPTY;
            }
        }));
        adapter = treeAdapterRef.get();

        if (mRootTreeAdapter == null) {
            mRootTreeAdapter = treeAdapterRef.get();
        }

        // Loading indicator
        adapter = adapter
                .append(asAdapter(R.layout.list_loading_item).showOnlyWhile(isTrue(depth != 0).and(isLoading(data))));

        // Empty message
        adapter = adapter.compose(appendEmptyMessage(data, R.layout.list_empty_item));

        return adapter;
    }

    @NonNull
    private ViewFactory directoryHeader(@NonNull final File file, final int depth) {
        return new ViewFactory() {
            @NonNull
            @Override
            public View create(@NonNull ViewGroup parent) {
                TextView headerView = (TextView) LayoutInflater.from(getActivity())
                        .inflate(android.R.layout.simple_list_item_1, mRecyclerView, false);
                headerView.setBackgroundColor(0x20FFFFFF);
                headerView.setText(formatDir(file, depth));
                headerView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                return headerView;
            }
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
