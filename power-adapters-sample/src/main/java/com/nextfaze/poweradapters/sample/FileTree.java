package com.nextfaze.poweradapters.sample;

import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.TreeAdapter;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.ListBindingAdapter;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.nextfaze.poweradapters.PowerAdapter.concat;
import static com.nextfaze.poweradapters.sample.Utils.emptyMessage;
import static com.nextfaze.poweradapters.sample.Utils.loadingIndicator;
import static java.util.Collections.singletonList;

final class FileTree {

    private static final Object VIEW_TYPE_FILE = new Object();
    private static final Object VIEW_TYPE_PEEK = new Object();

    @NonNull
    static PowerAdapter createAdapter(@Nullable Data<File> overrideData, @NonNull File file) {
        return createAdapter(overrideData, file, new Tree(), 0);
    }

    @NonNull
    private static PowerAdapter createAdapter(@Nullable Data<File> overrideData,
                                              @NonNull File file,
                                              @NonNull Tree tree,
                                              int depth) {
        // Use the supplied Data if specified, otherwise create Data for directory contents.
        Data<File> data = overrideData != null ? overrideData : new DirectoryData(file);

        // Binder for a file/directory item.
        Binder<File, FileView> binder = new AbstractBinder<File, FileView>(R.layout.file_tree_file_item) {
            @Override
            public void bindView(@NonNull File file, @NonNull FileView v, @NonNull Holder holder) {
                v.setFile(file);
                v.setDepth(depth);
                v.setClickable(file.isDirectory());
                v.setOnClickListener(file.isDirectory() ? v1 -> tree.toggle(file, Flag.EXPANDED) : null);
                v.setOnPeekListener(file.isDirectory() ? () -> tree.togglePeek(file) : null);
            }

            @NonNull
            @Override
            public Object getViewType(@NonNull File file, int position) {
                return VIEW_TYPE_FILE;
            }
        };

        PowerAdapter adapter = new DataBindingAdapter(data, binder)
                .compose(nest(position -> {
                    File f = data.get(position);
                    PowerAdapter childAdapter = createPeekAdapter(f, tree);
                    if (f.isDirectory()) {
                        PowerAdapter childFilesAdapter = createAdapter(null, f, tree, depth + 1)
                                .showOnlyWhile(tree.isSet(f, Flag.EXPANDED));
                        return childAdapter.append(childFilesAdapter);
                    } else {
                        return childAdapter;
                    }
                }));

        // Add loading indicator and empty message.
        return concat(adapter, loadingIndicator(data), emptyMessage(data));
    }

    @NonNull
    private static PowerAdapter createPeekAdapter(@NonNull File file, @NonNull Tree tree) {
        Binder<File, FilePeekView> peekBinder = new AbstractBinder<File, FilePeekView>(R.layout.file_tree_peek_item) {
            @Override
            public void bindView(@NonNull File file, @NonNull FilePeekView v, @NonNull Holder holder) {
                v.setFile(file);
            }

            @NonNull
            @Override
            public Object getViewType(@NonNull File file, int position) {
                return VIEW_TYPE_PEEK;
            }
        };
        return new ListBindingAdapter<>(peekBinder, singletonList(file)).showOnlyWhile(tree.isSet(file, Flag.PEEKING));
    }

    @NonNull
    private static PowerAdapter.Transformer nest(@NonNull TreeAdapter.ChildAdapterSupplier childAdapterSupplier) {
        return adapter -> {
            TreeAdapter treeAdapter = new TreeAdapter(adapter, childAdapterSupplier);
            treeAdapter.setAutoExpand(true);
            return treeAdapter;
        };
    }

    /** Contains tree metadata, which controls expansion/peeking. */
    private static final class Tree {

        @NonNull
        private final Map<File, EnumSet<Flag>> mFlags = new HashMap<>();

        @NonNull
        private final Set<FlagCondition> mConditions = new CopyOnWriteArraySet<>();

        void togglePeek(@NonNull File file) {
            for (Map.Entry<File, EnumSet<Flag>> entry : mFlags.entrySet()) {
                if (!entry.getKey().equals(file)) {
                    entry.getValue().remove(Flag.PEEKING);
                }
            }
            EnumSet<Flag> flags = flags(file);
            if (flags.contains(Flag.PEEKING)) {
                flags.remove(Flag.PEEKING);
            } else {
                flags.add(Flag.PEEKING);
            }
            dispatchChanged();
        }

        void toggle(@NonNull File file, @NonNull Flag flag) {
            EnumSet<Flag> flags = flags(file);
            if (flags.contains(flag)) {
                if (flags.remove(flag)) {
                    dispatchChanged();
                }
            } else {
                if (flags.add(flag)) {
                    dispatchChanged();
                }
            }
        }

        @NonNull
        private EnumSet<Flag> flags(@NonNull File file) {
            EnumSet<Flag> flags = mFlags.get(file);
            if (flags == null) {
                flags = EnumSet.noneOf(Flag.class);
                mFlags.put(file, flags);
            }
            return flags;
        }

        private void dispatchChanged() {
            for (FlagCondition condition : mConditions) {
                condition.notifyChangedInternal();
            }
        }

        @NonNull
        Condition isSet(@NonNull File file, @NonNull Flag flag) {
            return new FlagCondition() {
                @Override
                public boolean eval() {
                    Set<Flag> flags = mFlags.get(file);
                    return flags != null && flags.contains(flag);
                }
            };
        }

        private abstract class FlagCondition extends Condition {
            @Override
            protected void onFirstObserverRegistered() {
                mConditions.add(this);
            }

            @Override
            protected void onLastObserverUnregistered() {
                mConditions.remove(this);
            }

            void notifyChangedInternal() {
                notifyChanged();
            }
        }
    }

    /** Enum of possible flags each {@link File} in a {@link Tree} can have. */
    private enum Flag {
        EXPANDED, PEEKING
    }
}
