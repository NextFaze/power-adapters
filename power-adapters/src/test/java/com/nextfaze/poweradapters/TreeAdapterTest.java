package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.Truth.assertThat;
import static com.nextfaze.poweradapters.ArgumentMatchers.holderWithPosition;
import static com.nextfaze.poweradapters.PowerAdapter.EMPTY;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class TreeAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private FakeAdapter<Integer> mRootAdapter;
    private List<FakeAdapter<Integer>> mChildAdapters;
    private TreeAdapter mTreeAdapter;
    private ViewGroup mParent;
    private View mItemView;

    @Before
    public void setUp() throws Exception {
        mRootAdapter = spy(fakeAdapter(3));
        mChildAdapters = ImmutableList.of(
                spy(fakeAdapter(3)),
                spy(fakeAdapter(3)),
                spy(fakeAdapter(3))
        );
        mTreeAdapter = spy(new TreeAdapter(mRootAdapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return mChildAdapters.get(position);
            }
        });
        mParent = new FrameLayout(RuntimeEnvironment.application);
        mItemView = new View(RuntimeEnvironment.application);
    }

    @Test
    public void getItemIdIsTranslated() {
        getCallIsTranslated(GetCall.ITEM_ID);
    }

    @Test
    public void getItemViewTypeIsTranslated() {
        getCallIsTranslated(GetCall.ITEM_VIEW_TYPE);
    }

    @Test
    public void isEnabledIsTranslated() {
        getCallIsTranslated(GetCall.ENABLED);
    }

    private void getCallIsTranslated(@NonNull GetCall call) {
        PowerAdapter rootAdapter = spy(fakeAdapter(3));
        final List<PowerAdapter> childAdapters = new ArrayList<>();
        for (int i = 0; i < rootAdapter.getItemCount(); i++) {
            childAdapters.add(spy(fakeAdapter(3)));
        }
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return childAdapters.get(position);
            }
        };
        treeAdapter.setAllExpanded(true);

        new SubAdapterVerifier(treeAdapter, call,
                rootAdapter, childAdapters.get(0), childAdapters.get(1), childAdapters.get(2))
                .that(rootAdapter, 0)
                .that(childAdapters.get(0), 0)
                .that(childAdapters.get(0), 1)
                .that(childAdapters.get(0), 2)
                .that(rootAdapter, 1)
                .that(childAdapters.get(1), 0)
                .that(childAdapters.get(1), 1)
                .that(childAdapters.get(1), 2)
                .that(rootAdapter, 2)
                .that(childAdapters.get(2), 0)
                .that(childAdapters.get(2), 1)
                .that(childAdapters.get(2), 2)
                .verify();
    }

    @Test
    public void itemCountIncludesRootOnlyWhenNotExpanded() {
        assertThat(mTreeAdapter.getItemCount()).isEqualTo(3);
    }

    @Test
    public void itemCountIncludesRootAndChildrenWhenExpanded() {
        mTreeAdapter.setAllExpanded(true);
        assertThat(mTreeAdapter.getItemCount()).isEqualTo(12);
    }

    @Test
    public void childNewViewDelegation() {
        mTreeAdapter.setAllExpanded(true);
        ViewType viewType = mTreeAdapter.getItemViewType(3);
        PowerAdapter childAdapter = mChildAdapters.get(0);
        reset(childAdapter);
        mTreeAdapter.newView(mParent, viewType);
        verify(childAdapter).newView(mParent, viewType);
        verifyNoMoreInteractions(childAdapter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void rootNewViewDelegation() {
        mTreeAdapter.setAllExpanded(true);
        ViewType viewType = mTreeAdapter.getItemViewType(4);
        reset(mRootAdapter);
        mTreeAdapter.newView(mParent, viewType);
        verify(mRootAdapter).newView(mParent, viewType);
        verifyNoMoreInteractions(mRootAdapter);
    }

    @Test
    public void childBindViewDelegation() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.bindView(mItemView, new Holder() {
            @Override
            public int getPosition() {
                return 10;
            }
        });
        PowerAdapter childAdapter = mChildAdapters.get(2);
        verify(childAdapter).bindView(eq(mItemView), argThat(holderWithPosition(1)));
        verifyNoMoreInteractions(childAdapter);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void rootBindViewDelegation() {
        mTreeAdapter.setAllExpanded(true);
        reset(mRootAdapter);
        mTreeAdapter.bindView(mItemView, new Holder() {
            @Override
            public int getPosition() {
                return 8;
            }
        });
        verify(mRootAdapter).bindView(eq(mItemView), argThat(holderWithPosition(2)));
        verifyNoMoreInteractions(mRootAdapter);
    }

    @Test
    public void expandInsertsChildItems() {
        mTreeAdapter.registerDataObserver(mObserver);
        mTreeAdapter.setExpanded(1, true);
        verify(mObserver).onItemRangeInserted(2, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void expandInvokesGetChildAdapter() {
        mTreeAdapter.setExpanded(1, true);
        verify(mTreeAdapter).getChildAdapter(1);
    }

    @Test
    public void collapseRemovesChildItems() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mTreeAdapter.setExpanded(2, false);
        verify(mObserver).onItemRangeRemoved(9, 3);
        verifyNoMoreInteractions(mObserver);
    }

    // TODO: Check that after collapse, child notifications are ignored.

    @Test
    public void registerObserversOfChildAdapters() {
        final PowerAdapter childAdapter = mock(PowerAdapter.class);
        TreeAdapter treeAdapter = new TreeAdapter(fakeAdapter(3)) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return childAdapter;
            }
        };
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mock(DataObserver.class));
        verify(childAdapter, times(3)).registerDataObserver(any(DataObserver.class));
    }

    @Test
    public void unregisterObserversOfChildAdapters() {
        final List<PowerAdapter> childAdapters = ImmutableList.of(
                mock(PowerAdapter.class),
                mock(PowerAdapter.class),
                mock(PowerAdapter.class)
        );
        TreeAdapter treeAdapter = new TreeAdapter(mRootAdapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return childAdapters.get(position);
            }
        };
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        treeAdapter.unregisterDataObserver(mObserver);
        for (PowerAdapter adapter : childAdapters) {
            ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
            verify(adapter).registerDataObserver(captor.capture());
            verify(adapter).unregisterDataObserver(eq(captor.getValue()));
        }
    }

    @Test
    public void childInsertion() {
        final FakeAdapter<Integer> childAdapter = fakeAdapter(3);
        TreeAdapter treeAdapter = new TreeAdapter(fakeAdapter(1)) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return childAdapter;
            }
        };
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        childAdapter.add(5);
        verify(mObserver).onItemRangeInserted(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childRemoval() {
        final FakeAdapter<Integer> childAdapter = fakeAdapter(3);
        TreeAdapter treeAdapter = new TreeAdapter(fakeAdapter(1)) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return childAdapter;
            }
        };
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        childAdapter.remove(1);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    /** Special test case for checking for known crash bug. */
    @Test
    public void rootChangeThenRemoval() {
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.set(0, 0);
        mRootAdapter.remove(2);
        verify(mObserver).onItemRangeChanged(0, 1);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootChangePositionAccountsForExpandedChildren() {
        mTreeAdapter.setExpanded(1, true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.set(2, 0);
        verify(mObserver).onItemRangeChanged(5, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootChangeInvokesGetChildAdapter() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        reset(mTreeAdapter);
        mRootAdapter.set(1, 0);
        verify(mTreeAdapter).getChildAdapter(1);
    }

    @Test
    public void rootChangeUnregistersFromPreviousChildAdapterIfExpanded() {
        FakeAdapter<Integer> rootAdapter = fakeAdapter(1);
        PowerAdapter oldChildAdapter = mock(PowerAdapter.class);
        final AtomicReference<PowerAdapter> childAdapterRef = new AtomicReference<>(oldChildAdapter);
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return childAdapterRef.get();
            }
        };
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        // Set to return a different child adapter now.
        childAdapterRef.getAndSet(EMPTY);
        rootAdapter.set(0, 10);
        // Verify that the same registered observer was later unregistered.
        ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
        verify(oldChildAdapter).registerDataObserver(captor.capture());
        verify(oldChildAdapter).unregisterDataObserver(eq(captor.getValue()));
    }

    @Test
    public void rootChangeRemovesAndInsertsChildItemsIfExpandedAndAdapterChanges() {
        // TreeAdapter child adapters must change each invocation.
        TreeAdapter treeAdapter = spy(new TreeAdapter(mRootAdapter) {
            @NonNull
            @Override
            protected PowerAdapter getChildAdapter(int position) {
                return fakeAdapter(3);
            }
        });
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        mRootAdapter.set(1, 0);
        verify(mObserver).onItemRangeChanged(4, 1);
        verify(mObserver).onItemRangeRemoved(5, 3);
        verify(mObserver).onItemRangeInserted(5, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootInsertionPositionAccountsForExpandedChildren() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.add(1, 25);
        verify(mObserver).onItemRangeInserted(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootRemovalPositionAccountsForExpandedChildren() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.remove(1);
        verify(mObserver).onItemRangeRemoved(eq(4), anyInt());
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootRemovalRemovesExpandedChildItems() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.remove(1);
        verify(mObserver).onItemRangeRemoved(4, 4);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootMoveIsTranslatedAndIncludesExpandedChildren() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.move(1, 2, 1);
        verify(mObserver).onItemRangeMoved(4, 8, 4);
        verifyNoMoreInteractions(mObserver);
    }

    // TODO: After every type of root change, check that get*() calls map correctly.

    @Test
    public void childChangeIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).set(2, 0);
        verify(mObserver).onItemRangeChanged(11, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childInsertIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(1).add(5);
        verify(mObserver).onItemRangeInserted(8, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childRemoveIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(0).remove(1);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childMoveIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).move(0, 2, 1);
        verify(mObserver).onItemRangeMoved(9, 11, 1);
        verifyNoMoreInteractions(mObserver);
    }

    // TODO: After every type of child change, check that get*() calls map correctly.

    @Test
    public void internalStateIsValidWithinChangeNotification() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(new SimpleDataObserver() {
            @Override
            public void onChanged() {
                new SubAdapterVerifier(mTreeAdapter, mRootAdapter,
                        mChildAdapters.get(0), mChildAdapters.get(1), mChildAdapters.get(2))
                        .addAllGetCalls()
                        .that(mRootAdapter, 0)
                        .that(mChildAdapters.get(0), 0)
                        .that(mChildAdapters.get(0), 1)
                        .that(mChildAdapters.get(0), 2)
                        .that(mRootAdapter, 1)
                        .that(mRootAdapter, 2)
                        .that(mChildAdapters.get(2), 0)
                        .that(mChildAdapters.get(2), 1)
                        .that(mChildAdapters.get(2), 2)
                        .verify();
            }
        });
        mChildAdapters.get(1).clear();
    }

    // TODO: State save/restore tests.

    @NonNull
    private static FakeAdapter<Integer> fakeAdapter(int itemCount) {
        FakeAdapter<Integer> adapter = new FakeAdapter<>();
        for (int i = 0; i < itemCount; i++) {
            adapter.add(i);
        }
        return adapter;
    }

    private static final class SubAdapterVerifier {

        @NonNull
        private final TreeAdapter mTreeAdapter;

        @NonNull
        private final InOrder mInOrder;

        @NonNull
        private final Set<GetCall> mGetCalls = new HashSet<>();

        @NonNull
        private final List<Runnable> mChecks = new ArrayList<>();

        SubAdapterVerifier(@NonNull TreeAdapter treeAdapter, @NonNull PowerAdapter... mockAdapters) {
            mTreeAdapter = treeAdapter;
            mInOrder = inOrder(mockAdapters);
        }

        SubAdapterVerifier(@NonNull TreeAdapter treeAdapter,
                           @NonNull GetCall getCall,
                           @NonNull PowerAdapter... mockAdapters) {
            mTreeAdapter = treeAdapter;
            mInOrder = inOrder(mockAdapters);
            mGetCalls.add(getCall);
        }

        @NonNull
        SubAdapterVerifier addGetCall(@NonNull GetCall getCall) {
            mGetCalls.add(getCall);
            return this;
        }

        @NonNull
        SubAdapterVerifier addAllGetCalls() {
            for (GetCall call : GetCall.values()) {
                addGetCall(call);
            }
            return this;
        }

        /** Verify that the specified mocked sub adapter was invoked with the specified position arg. */
        @NonNull
        SubAdapterVerifier that(@NonNull final PowerAdapter mockAdapter, final int position) {
            mChecks.add(new Runnable() {
                @Override
                public void run() {
                    for (GetCall call : mGetCalls) {
                        call.get(mInOrder.verify(mockAdapter), position);
                    }
                }
            });
            return this;
        }

        /** Must be called at the end to perform the verification. */
        void verify() {
            checkState(!mGetCalls.isEmpty(), "Must specify at least one " + GetCall.class.getSimpleName());
            checkState(mTreeAdapter.getItemCount() >= mChecks.size(),
                    TreeAdapter.class.getSimpleName() +
                            " does not have enough items to perform the requested verifications");
            for (int i = 0; i < mChecks.size(); i++) {
                for (GetCall call : mGetCalls) {
                    call.get(mTreeAdapter, i);
                }
            }
            for (Runnable check : mChecks) {
                check.run();
            }
            mInOrder.verifyNoMoreInteractions();
        }
    }
}
