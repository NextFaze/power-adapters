package com.nextfaze.poweradapters;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Lists.newArrayList;
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

    private FakeAdapter mRootAdapter;
    private List<FakeAdapter> mChildAdapters;
    private TreeAdapter.ChildAdapterSupplier mChildAdapterSupplier;
    private TreeAdapter mTreeAdapter;
    private ViewGroup mParent;
    private View mItemView;

    @Before
    public void setUp() throws Exception {
        mRootAdapter = spy(new FakeAdapter(3));
        mChildAdapters = newArrayList(
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(3))
        );
        mChildAdapterSupplier = spy(new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return mChildAdapters.get(position);
            }
        });
        mTreeAdapter = new TreeAdapter(mRootAdapter, mChildAdapterSupplier);
        mParent = new FrameLayout(RuntimeEnvironment.application);
        mItemView = new View(RuntimeEnvironment.application);
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
    public void itemCountIsCorrectWhenPartiallyExpanded() {
        mTreeAdapter.setExpanded(1, true);
        assertThat(mTreeAdapter.getItemCount()).isEqualTo(6);
    }

    @Test
    public void expansionStateIsCorrect0() {
        mTreeAdapter.setExpanded(0, true);
        reset(mRootAdapter, mChildAdapters.get(0), mChildAdapters.get(1), mChildAdapters.get(2));
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mChildAdapters.get(0), 0)
                .check(mChildAdapters.get(0), 1)
                .check(mChildAdapters.get(0), 2)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2)
                .verify(mTreeAdapter);
    }

    @Test
    public void expansionStateIsCorrect1() {
        mTreeAdapter.setExpanded(1, true);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mRootAdapter, 1)
                .check(mChildAdapters.get(1), 0)
                .check(mChildAdapters.get(1), 1)
                .check(mChildAdapters.get(1), 2)
                .check(mRootAdapter, 2)
                .verify(mTreeAdapter);
    }

    @Test
    public void expansionStateIsCorrect2() {
        mTreeAdapter.setExpanded(2, true);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2)
                .check(mChildAdapters.get(2), 0)
                .check(mChildAdapters.get(2), 1)
                .check(mChildAdapters.get(2), 2)
                .verify(mTreeAdapter);
    }

    @Test
    public void expandInsertsChildItems() {
        mTreeAdapter.registerDataObserver(mObserver);
        mTreeAdapter.setExpanded(1, true);
        mTreeAdapter.setExpanded(0, true);
        mTreeAdapter.setExpanded(2, true);
        verify(mObserver).onItemRangeInserted(2, 3);
        verify(mObserver).onItemRangeInserted(1, 3);
        verify(mObserver).onItemRangeInserted(9, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void expandInvokesGetChildAdapter() {
        mTreeAdapter.setExpanded(0, true);
        mTreeAdapter.setExpanded(1, true);
        mTreeAdapter.setExpanded(2, true);
        verify(mChildAdapterSupplier).get(0);
        verify(mChildAdapterSupplier).get(1);
        verify(mChildAdapterSupplier).get(2);
        verifyNoMoreInteractions(mChildAdapterSupplier);
    }

    @Test
    public void collapseRemovesChildItems() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mTreeAdapter.setExpanded(2, false);
        verify(mObserver).onItemRangeRemoved(9, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void collapseStateIsCorrect0() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.setExpanded(0, false);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mRootAdapter, 1)
                .check(mChildAdapters.get(1), 0)
                .check(mChildAdapters.get(1), 1)
                .check(mChildAdapters.get(1), 2)
                .check(mRootAdapter, 2)
                .check(mChildAdapters.get(2), 0)
                .check(mChildAdapters.get(2), 1)
                .check(mChildAdapters.get(2), 2)
                .verify(mTreeAdapter);
    }

    @Test
    public void collapseStateIsCorrect1() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.setExpanded(1, false);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mChildAdapters.get(0), 0)
                .check(mChildAdapters.get(0), 1)
                .check(mChildAdapters.get(0), 2)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2)
                .check(mChildAdapters.get(2), 0)
                .check(mChildAdapters.get(2), 1)
                .check(mChildAdapters.get(2), 2)
                .verify(mTreeAdapter);
    }

    @Test
    public void collapseStateIsCorrect2() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.setExpanded(2, false);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mChildAdapters.get(0), 0)
                .check(mChildAdapters.get(0), 1)
                .check(mChildAdapters.get(0), 2)
                .check(mRootAdapter, 1)
                .check(mChildAdapters.get(1), 0)
                .check(mChildAdapters.get(1), 1)
                .check(mChildAdapters.get(1), 2)
                .check(mRootAdapter, 2)
                .verify(mTreeAdapter);
    }

    @Test
    public void childNotificationsAfterIgnoredAfterCollapse() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.setExpanded(2, false);
        mTreeAdapter.setExpanded(0, false);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(0).remove(0, 1);
        mChildAdapters.get(2).insert(1, 1);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void treeRegistersObserversOnChildAdaptersWhenFirstExternalObserverRegisters() {
        final PowerAdapter childAdapter = mock(PowerAdapter.class);
        TreeAdapter treeAdapter = new TreeAdapter(new FakeAdapter(3), new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mock(DataObserver.class));
        verify(childAdapter, times(3)).registerDataObserver(any(DataObserver.class));
    }

    @Test
    public void treeUnregisterObserversFromChildAdaptersWhenLastExternalObserverUnregisters() {
        final List<PowerAdapter> childAdapters = ImmutableList.of(
                mock(PowerAdapter.class),
                mock(PowerAdapter.class),
                mock(PowerAdapter.class)
        );
        TreeAdapter treeAdapter = new TreeAdapter(mRootAdapter, new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapters.get(position);
            }
        });
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        treeAdapter.unregisterDataObserver(mObserver);
        for (PowerAdapter adapter : childAdapters) {
            ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
            verify(adapter).registerDataObserver(captor.capture());
            verify(adapter).unregisterDataObserver(eq(captor.getValue()));
        }
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

    /** Special test case for checking for known crash bug. */
    @Test
    public void rootChangeThenRemovalDoesNotThrow() {
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.change(0, 1);
        mRootAdapter.remove(2, 1);
        verify(mObserver).onItemRangeChanged(0, 1);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootChangePositionAccountsForExpandedChildren() {
        mTreeAdapter.setExpanded(1, true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.change(2, 1);
        verify(mObserver).onItemRangeChanged(5, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootChangeInvokesGetChildAdapterAgain() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        reset(mChildAdapterSupplier);
        mRootAdapter.change(1, 1);
        verify(mChildAdapterSupplier).get(1);
        verifyNoMoreInteractions(mChildAdapterSupplier);
    }

    @Test
    public void rootChangeUnregistersFromPreviousChildAdapterIfExpanded() {
        FakeAdapter rootAdapter = new FakeAdapter(1);
        PowerAdapter oldChildAdapter = mock(PowerAdapter.class);
        final AtomicReference<PowerAdapter> childAdapterRef = new AtomicReference<>(oldChildAdapter);
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapterRef.get();
            }
        });
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        // Set to return a different child adapter now.
        childAdapterRef.getAndSet(EMPTY);
        rootAdapter.change(0, 1);
        // Verify that the same registered observer was later unregistered.
        ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
        verify(oldChildAdapter).registerDataObserver(captor.capture());
        verify(oldChildAdapter).unregisterDataObserver(eq(captor.getValue()));
    }

    @Test
    public void rootChangeRemovesAndInsertsChildItemsIfExpandedAndAdapterChanges() {
        // TreeAdapter child adapters must change each invocation for this test.
        TreeAdapter treeAdapter = new TreeAdapter(mRootAdapter, new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return new FakeAdapter(3);
            }
        });
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        mRootAdapter.change(1, 1);
        verify(mObserver).onItemRangeChanged(4, 1);
        verify(mObserver).onItemRangeRemoved(5, 3);
        verify(mObserver).onItemRangeInserted(5, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootChangeStateIsCorrect() {
        mTreeAdapter.setAllExpanded(true);
        mRootAdapter.change(1, 1);
        mRootAdapter.change(0, 1);
        mRootAdapter.change(2, 1);
        verifyStateAllExpanded();
    }

    @Test
    public void rootInsertionPositionAccountsForExpandedChildren() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.insert(1, 1);
        verify(mObserver).onItemRangeInserted(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootInsertionStateIsCorrect() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        resetAll();
        mChildAdapters.add(1, spy(new FakeAdapter(3)));
        mRootAdapter.insert(1, 1);
        mChildAdapters.add(1, spy(new FakeAdapter(3)));
        mRootAdapter.insert(1, 1);
        mChildAdapters.add(1, spy(new FakeAdapter(3)));
        mRootAdapter.insert(1, 1);
        AdapterVerifier.verifySubAdapterCalls(GetCall.ENABLED)
                .check(mRootAdapter, 0)
                .check(mChildAdapters.get(0), 0)
                .check(mChildAdapters.get(0), 1)
                .check(mChildAdapters.get(0), 2)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2) // TODO: Skipped for some reason.
                .check(mRootAdapter, 3) // TODO: Skipped for some reason.
                .check(mRootAdapter, 4) // TODO: Skipped for some reason.
                .check(mChildAdapters.get(4), 0)
                .check(mChildAdapters.get(4), 1)
                .check(mChildAdapters.get(4), 2)
                .check(mRootAdapter, 5)
                .check(mChildAdapters.get(5), 0)
                .check(mChildAdapters.get(5), 1)
                .check(mChildAdapters.get(5), 2)
                .verify(mTreeAdapter);
    }

    @Test
    public void rootRemovalPositionAccountsForExpandedChildren() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.remove(1, 1);
        verify(mObserver).onItemRangeRemoved(eq(4), anyInt());
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootRemovalRemovesExpandedChildItems() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.remove(1, 1);
        verify(mObserver).onItemRangeRemoved(4, 4);
        verifyNoMoreInteractions(mObserver);
    }

    @Ignore
    @Test
    public void rootMoveIsTranslatedAndIncludesExpandedChildren() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.move(1, 2, 1);
        verify(mObserver).onItemRangeMoved(4, 8, 4);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void rootMoveNotifiesOfChange() {
        mTreeAdapter.registerDataObserver(mObserver);
        mRootAdapter.move(1, 2, 1);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    // TODO: After every type of root change, check that get*() calls map correctly.

    @Test
    public void childNewViewDelegation() {
        mTreeAdapter.setAllExpanded(true);
        ViewType viewType = mTreeAdapter.getItemViewType(3);
        PowerAdapter childAdapter = mChildAdapters.get(0);
        mTreeAdapter.newView(mParent, viewType);
        verify(childAdapter).newView(mParent, viewType);
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
    }

    @Test
    public void childInsertionGeneratesTreeInsertion() {
        final FakeAdapter childAdapter = new FakeAdapter(3);
        TreeAdapter treeAdapter = new TreeAdapter(new FakeAdapter(1), new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        childAdapter.append(2);
        verify(mObserver).onItemRangeInserted(4, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childRemovalGeneratesTreeRemoval() {
        final FakeAdapter childAdapter = new FakeAdapter(3);
        TreeAdapter treeAdapter = new TreeAdapter(new FakeAdapter(1), new TreeAdapter.ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        treeAdapter.setAllExpanded(true);
        treeAdapter.registerDataObserver(mObserver);
        childAdapter.remove(1, 1);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childChangeIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).change(2, 1);
        verify(mObserver).onItemRangeChanged(11, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childChangeWhileNotExpandedIsIgnored() {
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).change(2, 1);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void childInsertIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(1).append(1);
        verify(mObserver).onItemRangeInserted(8, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childInsertWhileNotExpandedIsIgnored() {
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).append(4);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void childRemoveIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(0).remove(1, 1);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childRemoveWhileNotExpandedIsIgnored() {
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(0).remove(1, 2);
        verifyZeroInteractions(mObserver);
    }

    @Ignore
    @Test
    public void childMoveIsTranslated() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).move(0, 2, 1);
        verify(mObserver).onItemRangeMoved(9, 11, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childMoveNotifiesOfChange() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).move(0, 2, 1);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childMoveWhileNotExpandedIsIgnored() {
        mTreeAdapter.registerDataObserver(mObserver);
        mChildAdapters.get(2).move(1, 2, 1);
        verifyZeroInteractions(mObserver);
    }

    // TODO: After every type of child change, check that get*() calls map correctly.

    @Test
    public void internalStateIsValidWithinChangeNotification() {
        mTreeAdapter.setAllExpanded(true);
        mTreeAdapter.registerDataObserver(new SimpleDataObserver() {
            @Override
            public void onChanged() {
                AdapterVerifier.verifySubAdapterAllGetCalls()
                        .check(mRootAdapter, 0)
                        .check(mChildAdapters.get(0), 0)
                        .check(mChildAdapters.get(0), 1)
                        .check(mChildAdapters.get(0), 2)
                        .check(mRootAdapter, 1)
                        .check(mRootAdapter, 2)
                        .check(mChildAdapters.get(2), 0)
                        .check(mChildAdapters.get(2), 1)
                        .check(mChildAdapters.get(2), 2)
                        .verify(mTreeAdapter);
            }
        });
        mChildAdapters.get(1).clear();
        // TODO: Perform this check for each type of change.
        // TODO: Also verify that a change notification was delivered at all, otherwise the above checks won't even be performed.
    }

    @Test
    public void saveRestoreState() {
        mTreeAdapter.registerDataObserver(mObserver);
        mTreeAdapter.setExpanded(0, true);
        mTreeAdapter.setExpanded(2, true);
        verifySavedState(mTreeAdapter);
        Parcelable state = mTreeAdapter.saveInstanceState();
        TreeAdapter treeAdapter = new TreeAdapter(mRootAdapter, mChildAdapterSupplier);
        treeAdapter.registerDataObserver(mObserver);
        resetAll();
        treeAdapter.restoreInstanceState(state);
        verifySavedState(treeAdapter);
    }

    private void verifySavedState(@NonNull TreeAdapter treeAdapter) {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mChildAdapters.get(0), 0)
                .check(mChildAdapters.get(0), 1)
                .check(mChildAdapters.get(0), 2)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2)
                .check(mChildAdapters.get(2), 0)
                .check(mChildAdapters.get(2), 1)
                .check(mChildAdapters.get(2), 2)
                .verify(treeAdapter);
    }

    private void verifyStateNoneExpanded() {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2)
                .verify(mTreeAdapter);
    }

    private void verifyStateAllExpanded() {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mChildAdapters.get(0), 0)
                .check(mChildAdapters.get(0), 1)
                .check(mChildAdapters.get(0), 2)
                .check(mRootAdapter, 1)
                .check(mChildAdapters.get(1), 0)
                .check(mChildAdapters.get(1), 1)
                .check(mChildAdapters.get(1), 2)
                .check(mRootAdapter, 2)
                .check(mChildAdapters.get(2), 0)
                .check(mChildAdapters.get(2), 1)
                .check(mChildAdapters.get(2), 2)
                .verify(mTreeAdapter);
    }

    private void resetAll() {
        reset(mRootAdapter);
        for (PowerAdapter adapter : mChildAdapters) {
            reset(adapter);
        }
    }
}
