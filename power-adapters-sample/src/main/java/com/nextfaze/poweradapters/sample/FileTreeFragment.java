package com.nextfaze.poweradapters.sample;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.DividerAdapterBuilder;
import com.nextfaze.poweradapters.EmptyAdapterBuilder;
import com.nextfaze.poweradapters.HeaderAdapterBuilder;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.LoadingAdapterBuilder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.TreeAdapter;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.asyncdata.DataBindingAdapter;
import com.nextfaze.poweradapters.asyncdata.DataEmptyDelegate;
import com.nextfaze.poweradapters.asyncdata.DataLoadingDelegate;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.TypedBinder;
import lombok.NonNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static android.graphics.Typeface.DEFAULT;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static android.support.v7.widget.LinearLayoutManager.VERTICAL;
import static com.google.common.base.Strings.repeat;
import static com.nextfaze.poweradapters.DividerAdapterBuilder.EmptyPolicy.SHOW_LEADING;
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public class FileTreeFragment extends BaseFragment {

    private static final int MAX_DISPLAYED_FILES_PER_DIR = Integer.MAX_VALUE;

    @NonNull
    private final File mRootFile = new File("/");

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

    @Nullable
    private Data<File> mRootData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Async adapter.
        PowerAdapter adapter = createFilesAdapter(mRootFile, 0);
        mDataLayout.setData(mRootData);

        // Simple, non-async adapter.
//        PowerAdapter adapter = createFilesAdapterSimple(mRootFile, 0, true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), VERTICAL, false));
        mRecyclerView.setAdapter(toRecyclerAdapter(adapter));
        showCollectionView(CollectionView.RECYCLER_VIEW);
    }

    @NonNull
    private PowerAdapter createFilesAdapterSimple(@NonNull final File file, final int depth, final boolean tree) {
        final AtomicReference<TreeAdapter> treeAdapterRef = new AtomicReference<>();
        Binder binder = new TypedBinder<File, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            protected void bind(@NonNull final File file, @NonNull TextView v, @NonNull final Holder holder) {
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
        final List<File> files = FluentIterable.from(filesArray != null ? Lists.newArrayList(filesArray) : Collections.<File>emptyList())
                .limit(MAX_DISPLAYED_FILES_PER_DIR)
                .toList();
        PowerAdapter adapter = new FileAdapter(files, singletonMapper(binder));
        treeAdapterRef.set(new TreeAdapter(adapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                File file = files.get(position);
                return createFilesAdapterSimple(file, depth + 1, true);
            }
        });
        if (tree) {
            adapter = treeAdapterRef.get();
        }

        adapter = new HeaderAdapterBuilder()
                .addView(directoryHeader(file, depth))
                .build(adapter);

        if (depth == 1) {
            adapter = new DividerAdapterBuilder()
                    .innerResource(R.layout.list_divider_item)
                    .emptyPolicy(SHOW_LEADING)
                    .build(adapter);
        }

        return adapter;
    }

    @NonNull
    private PowerAdapter createFilesAdapter(@NonNull final File file, final int depth) {
        final Data<File> data = new DirectoryData(file, MAX_DISPLAYED_FILES_PER_DIR);
        if (mRootData == null) {
            mRootData = data;
            mDataLayout.setData(data);
        }

        final AtomicReference<TreeAdapter> treeAdapterRef = new AtomicReference<>();

        Binder binder = new TypedBinder<File, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            protected void bind(@NonNull final File file, @NonNull TextView v, @NonNull final Holder holder) {
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
        treeAdapterRef.set(new TreeAdapter(adapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return createFilesAdapter(data.get(position), depth + 1);
            }
        });
        adapter = treeAdapterRef.get();

        adapter = new LoadingAdapterBuilder()
                .resource(R.layout.list_loading_item)
                .emptyPolicy(depth == 0 ? LoadingAdapterBuilder.EmptyPolicy.SHOW_ONLY_IF_NON_EMPTY :
                        LoadingAdapterBuilder.EmptyPolicy.SHOW_ALWAYS)
                .build(adapter, new DataLoadingDelegate(data));

        adapter = new EmptyAdapterBuilder()
                .resource(R.layout.file_list_empty_item)
                .build(adapter, new DataEmptyDelegate(data));

        adapter = new HeaderAdapterBuilder()
                .addView(directoryHeader(file, depth))
                .build(adapter);

        adapter = new DataLifecycleAdapter(adapter, data);

//        if (depth == 1) {
//            adapter = new DividerAdapterBuilder()
//                    .innerResource(R.layout.list_divider_item)
//                    .leadingResource(R.layout.list_divider_item)
//                    .trailingResource(R.layout.list_divider_item)
//                    .emptyPolicy(SHOW_LEADING)
//                    .build(adapter);
//        }

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

    @NonNull
    private static String formatFile(@NonNull File file, int depth) {
        return repeat("    ", depth) + (file.isDirectory() ? file.getName() + "/" : file.getName());
    }

    @NonNull
    private static String formatDir(@NonNull File dir, int depth) {
        return repeat("    ", depth) + dir.getName() + ":";
    }
}
