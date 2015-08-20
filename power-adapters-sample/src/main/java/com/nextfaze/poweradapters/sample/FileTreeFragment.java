package com.nextfaze.poweradapters.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import com.google.common.collect.Lists;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.widget.DataLayout;
import com.nextfaze.poweradapters.EmptyAdapterBuilder;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.TreeAdapter;
import com.nextfaze.poweradapters.asyncdata.DataBindingAdapter;
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
import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.recyclerview.RecyclerPowerAdapters.toRecyclerAdapter;

public class FileTreeFragment extends BaseFragment {

    @Bind(R.id.data_layout)
    DataLayout mDataLayout;

    @Bind(R.id.recycler)
    RecyclerView mRecyclerView;

    @NonNull
    private Data<File> mRootData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mRootData = new DirectoryData(new File("/"), 3);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PowerAdapter adapter = createFilesAdapter(mRootData, 0);
//        PowerAdapter adapter = createFilesAdapter(new File("/"), 0);
        mDataLayout.setData(mRootData);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), VERTICAL, false));
        mRecyclerView.setAdapter(toRecyclerAdapter(adapter));
        showCollectionView(CollectionView.RECYCLER_VIEW);
    }

    @NonNull
    private PowerAdapter createFilesAdapter2(@NonNull final File file, final int depth) {
        final AtomicReference<TreeAdapter> treeAdapterRef = new AtomicReference<>();
        Binder binder = new TypedBinder<File, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            protected void bind(@NonNull final File file, @NonNull TextView v, @NonNull final Holder holder) {
                String label = file.isDirectory() ? file.getName() + "/" : file.getName();
                v.setText(repeat("    ", depth) + label);
                v.setTypeface(file.isDirectory() ? DEFAULT_BOLD : DEFAULT);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (file.isDirectory()) {
                            treeAdapterRef.get().toggleExpanded(holder.getPosition());
                        }
                    }
                });
            }
        };
        File[] filesArray = file.listFiles();
        final List<File> files = filesArray != null ? Lists.newArrayList(filesArray) : Collections.<File>emptyList();
        PowerAdapter adapter = new FileAdapter(files, singletonMapper(binder));
        treeAdapterRef.set(new TreeAdapter(adapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                File file = files.get(position);
                return createFilesAdapter2(file, depth + 1);
            }
        });
        adapter = treeAdapterRef.get();
        adapter = new EmptyAdapterBuilder()
                .resource(R.layout.file_list_empty_item)
                .build(adapter);
        return adapter;
    }

    @NonNull
    private PowerAdapter createFilesAdapter(@NonNull final Data<File> data, final int depth) {
        final AtomicReference<TreeAdapter> treeAdapterRef = new AtomicReference<>();
        Binder binder = new TypedBinder<File, TextView>(android.R.layout.simple_list_item_1) {
            @Override
            protected void bind(@NonNull final File file, @NonNull TextView v, @NonNull final Holder holder) {
                String label = file.isDirectory() ? file.getName() + "/" : file.getName();
                v.setText(repeat("    ", depth) + label);
                v.setTypeface(file.isDirectory() ? DEFAULT_BOLD : DEFAULT);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (file.isDirectory()) {
                            treeAdapterRef.get().toggleExpanded(holder.getPosition());
                        }
                    }
                });
            }
        };
        PowerAdapter adapter = new DataBindingAdapter(data, singletonMapper(binder));
        treeAdapterRef.set(new TreeAdapter(adapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                File file = data.get(position);
                Data<File> data = new DirectoryData(file, 3);
                // TODO: Call Show/hide from an adapter, based on registered observers?
                data.notifyShown();
                return createFilesAdapter(data, depth + 1);
            }
        });
        adapter = treeAdapterRef.get();
//        adapter = new LoadingAdapterBuilder()
//                .resource(R.layout.list_loading_item)
//                .build(adapter, new DataLoadingDelegate(data));
//        adapter = new EmptyAdapterBuilder()
//                .resource(R.layout.file_list_empty_item)
//                .build(adapter, new DataEmptyDelegate(data));
        return adapter;
    }
}
