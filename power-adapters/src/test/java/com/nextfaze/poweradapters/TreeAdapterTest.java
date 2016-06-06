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
import static com.nextfaze.poweradapters.AdapterTestUtils.*;
import static com.nextfaze.poweradapters.AdapterVerifier.verifySubAdapterAllGetCalls;
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
        mTreeAdapter.registerDataObserver(mObserver);
        mTreeAdapter.setAllExpanded(true);
        mParent = new FrameLayout(RuntimeEnvironment.application);
        mItemView = new View(RuntimeEnvironment.application);
    }

    @Test
    public void itemCountIncludesRootOnlyWhenNotExpanded() {
        mTreeAdapter.setAllExpanded(false);
        assertThat(mTreeAdapter.getItemCount()).isEqualTo(3);
    }

    @Test
    public void itemCountIncludesRootAndChildrenWhenExpanded() {
        mTreeAdapter.setAllExpanded(true);
        assertThat(mTreeAdapter.getItemCount()).isEqualTo(12);
    }

    @Test
    public void itemCountIsCorrectWhenPartiallyExpanded() {
        mTreeAdapter.setAllExpanded(false);
        mTreeAdapter.setExpanded(1, true);
        assertThat(mTreeAdapter.getItemCount()).isEqualTo(6);
    }

    @Test
    public void expansionStateIsCorrect0() {
        mTreeAdapter.setAllExpanded(false);
        mTreeAdapter.setExpanded(0, true);
        verifySubAdapterAllGetCalls()
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
        mTreeAdapter.setAllExpanded(false);
        mTreeAdapter.setExpanded(1, true);
        verifySubAdapterAllGetCalls()
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
        mTreeAdapter.setAllExpanded(false);
        mTreeAdapter.setExpanded(2, true);
        verifySubAdapterAllGetCalls()
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
        mTreeAdapter.setAllExpanded(false);
        DataObserver observer = registerMockDataObserver();
        mTreeAdapter.setExpanded(1, true);
        mTreeAdapter.setExpanded(0, true);
        mTreeAdapter.setExpanded(2, true);
        verify(observer).onItemRangeInserted(2, 3);
        verify(observer).onItemRangeInserted(1, 3);
        verify(observer).onItemRangeInserted(9, 3);
        verifyNoMoreInteractions(observer);
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
        DataObserver observer = registerMockDataObserver();
        mTreeAdapter.setExpanded(2, false);
        verify(observer).onItemRangeRemoved(9, 3);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void collapseStateIsCorrect0() {
        mTreeAdapter.setExpanded(0, false);
        verifySubAdapterAllGetCalls()
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
        mTreeAdapter.setExpanded(1, false);
        verifySubAdapterAllGetCalls()
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
        mTreeAdapter.setExpanded(2, false);
        verifySubAdapterAllGetCalls()
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
    public void childNotificationsAreIgnoredWhenCollapsed() {
        DataObserver observer = mock(DataObserver.class);
        mTreeAdapter.setExpanded(2, false);
        mTreeAdapter.setExpanded(0, false);
        mTreeAdapter.registerDataObserver(observer);
        mChildAdapters.get(0).remove(0, 1);
        mChildAdapters.get(2).insert(1, 1);
        verifyZeroInteractions(observer);
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

    @Test
    public void rootNewViewDelegation() {
        ViewType viewType = mTreeAdapter.getItemViewType(4);
        mTreeAdapter.newView(mParent, viewType);
        verify(mRootAdapter).newView(mParent, viewType);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void rootBindViewDelegation() {
        mTreeAdapter.bindView(mItemView, holder(8));
        verify(mRootAdapter).bindView(eq(mItemView), argThat(holderWithPosition(2)));
    }

    /** Special test case for checking for known crash bug. */
    @Test
    public void rootChangeThenRemovalDoesNotThrow() {
        mTreeAdapter.setAllExpanded(false);
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.change(0, 1);
        mRootAdapter.remove(2, 1);
        verify(observer).onItemRangeChanged(0, 1);
        verify(observer).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void rootChangePositionAccountsForExpandedChildren() {
        mTreeAdapter.setAllExpanded(false);
        mTreeAdapter.setExpanded(1, true);
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.change(2, 1);
        verify(observer).onItemRangeChanged(5, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void rootChangeInvokesGetChildAdapterAgain() {
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
        DataObserver observer = mock(DataObserver.class);
        treeAdapter.registerDataObserver(observer);
        mRootAdapter.change(1, 1);
        verify(observer).onItemRangeChanged(4, 1);
        verify(observer).onItemRangeRemoved(5, 3);
        verify(observer).onItemRangeInserted(5, 3);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void rootChangeStateIsCorrect() {
        mRootAdapter.change(1, 1);
        mRootAdapter.change(0, 1);
        mRootAdapter.change(2, 1);
        verifyStateAllExpanded();
    }

    @Test
    public void rootInsertionPositionAccountsForExpandedChildren() {
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.insert(1, 1);
        verify(observer).onItemRangeInserted(4, 1);
        verifyNoMoreInteractions(observer);
    }

    @Ignore
    @Test
    public void rootInsertionStateIsCorrect() {
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
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.remove(1, 1);
        verify(observer).onItemRangeRemoved(eq(4), anyInt());
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void rootRemovalRemovesExpandedChildItems() {
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.remove(1, 1);
        verify(observer).onItemRangeRemoved(4, 4);
        verifyNoMoreInteractions(observer);
    }

    @Ignore
    @Test
    public void rootMoveIsTranslatedAndIncludesExpandedChildren() {
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.move(1, 2, 1);
        verify(observer).onItemRangeMoved(4, 8, 4);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void rootMoveNotifiesOfChange() {
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.move(1, 2, 1);
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    // TODO: After every type of root change, check that get*() calls map correctly.

    @Test
    public void childNewViewDelegated() {
        ViewType viewType = mTreeAdapter.getItemViewType(3);
        mTreeAdapter.newView(mParent, viewType);
        verify(mChildAdapters.get(0)).newView(mParent, viewType);
        verifyNewViewNeverCalled(mChildAdapters.get(1));
        verifyNewViewNeverCalled(mChildAdapters.get(2));
    }

    @Test
    public void childBindViewDelegated() {
        mTreeAdapter.bindView(mItemView, holder(10));
        verifyBindViewNeverCalled(mChildAdapters.get(0));
        verifyBindViewNeverCalled(mChildAdapters.get(1));
        verify(mChildAdapters.get(2)).bindView(eq(mItemView), argThat(holderWithPosition(1)));
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
        DataObserver observer = mock(DataObserver.class);
        treeAdapter.registerDataObserver(observer);
        childAdapter.append(2);
        verify(observer).onItemRangeInserted(4, 2);
        verifyNoMoreInteractions(observer);
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
        DataObserver observer = mock(DataObserver.class);
        treeAdapter.registerDataObserver(observer);
        childAdapter.remove(1, 1);
        verify(observer).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childChangeIsTranslated() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).change(2, 1);
        verify(observer).onItemRangeChanged(11, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childChangeWhileNotExpandedIsIgnored() {
        mTreeAdapter.setAllExpanded(false);
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).change(2, 1);
        verifyZeroInteractions(observer);
    }

    @Test
    public void childInsertIsTranslated() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(1).append(1);
        verify(observer).onItemRangeInserted(8, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childInsertWhileNotExpandedIsIgnored() {
        mTreeAdapter.setAllExpanded(false);
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).append(4);
        verifyZeroInteractions(observer);
    }

    @Test
    public void childRemoveIsTranslated() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(0).remove(1, 1);
        verify(observer).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childRemoveWhileNotExpandedIsIgnored() {
        mTreeAdapter.setAllExpanded(false);
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(0).remove(1, 2);
        verifyZeroInteractions(observer);
    }

    @Ignore
    @Test
    public void childMoveIsTranslated() {
        mChildAdapters.get(2).move(0, 2, 1);
        verify(mObserver).onItemRangeMoved(9, 11, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childMoveNotifiesOfChange() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).move(0, 2, 1);
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childMoveWhileNotExpandedIsIgnored() {
        mTreeAdapter.setAllExpanded(false);
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).move(1, 2, 1);
        verifyZeroInteractions(observer);
    }

    // TODO: After every type of child change, check that get*() calls map correctly.

    @Test
    public void internalStateIsValidWithinChangeNotification() {
        mTreeAdapter.registerDataObserver(new SimpleDataObserver() {
            @Override
            public void onChanged() {
                verifySubAdapterAllGetCalls()
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
        verifySubAdapterAllGetCalls()
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
        verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2)
                .verify(mTreeAdapter);
    }

    private void verifyStateAllExpanded() {
        verifySubAdapterAllGetCalls()
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

    @NonNull
    private DataObserver registerMockDataObserver() {
        DataObserver observer = mock(DataObserver.class);
        mTreeAdapter.registerDataObserver(observer);
        return observer;
    }
}
