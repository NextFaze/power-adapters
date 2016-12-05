package com.nextfaze.poweradapters;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.common.collect.ImmutableList;
import com.nextfaze.poweradapters.TreeAdapter.ChildAdapterSupplier;
import lombok.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static com.nextfaze.poweradapters.AdapterTestUtils.*;
import static com.nextfaze.poweradapters.AdapterVerifier.verifySubAdapterAllGetCalls;
import static com.nextfaze.poweradapters.ArgumentMatchers.holderWithPosition;
import static com.nextfaze.poweradapters.PowerAdapter.EMPTY;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class TreeAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private FakeAdapter mRootAdapter;
    private List<FakeAdapter> mChildAdapters;
    private ChildAdapterSupplier mChildAdapterSupplier;
    private TreeAdapter mTreeAdapter;
    private ViewGroup mParent;
    private ViewGroup mContainerViewGroup;
    private View mItemView;

    @Mock
    private Container mContainer;

    private VerifyingAdapterObserver mVerifyingObserver;

    @Before
    public void setUp() throws Exception {
        mRootAdapter = spy(new FakeAdapter(3));
        mChildAdapters = newArrayList(
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(3))
        );
        mChildAdapterSupplier = spy(new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return mChildAdapters.get(position);
            }
        });
        mTreeAdapter = new TreeAdapter(mRootAdapter, mChildAdapterSupplier);
        mTreeAdapter.registerDataObserver(mObserver);
        mVerifyingObserver = new VerifyingAdapterObserver(mTreeAdapter);
        mTreeAdapter.registerDataObserver(mVerifyingObserver);
        mTreeAdapter.setAllExpanded(true);
        mParent = new FrameLayout(RuntimeEnvironment.application);
        mContainerViewGroup = new FrameLayout(RuntimeEnvironment.application);
        mItemView = new View(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        mVerifyingObserver.assertItemCountConsistent();
    }

    @Test
    public void isExpandedReturnsTrueWhenChildIsExpanded() {
        assertThat(mTreeAdapter.isExpanded(1)).isTrue();
    }

    @Test
    public void isExpandedReturnsFalseWhenChildIsNotExpanded() {
        mTreeAdapter.setAllExpanded(false);
        assertThat(mTreeAdapter.isExpanded(2)).isFalse();
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
    public void itemCountIsConsistentWithPreExpandedPosition() {
        final FakeAdapter childAdapter = new FakeAdapter(10);
        FakeLongAdapter rootAdapter = new FakeLongAdapter();
        rootAdapter.add(5L);
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        treeAdapter.setExpanded(0, true);
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(treeAdapter);
        treeAdapter.registerDataObserver(verifyingObserver);
        assertThat(treeAdapter.getItemCount()).isEqualTo(11);
        verifyingObserver.assertItemCountConsistent();
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
        TreeAdapter treeAdapter = new TreeAdapter(new FakeAdapter(3), new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        treeAdapter.setAutoExpand(true);
        treeAdapter.registerDataObserver(mock(DataObserver.class));
        verify(childAdapter, times(3)).registerDataObserver(any(DataObserver.class));
    }

    @Test
    public void treeUnregistersObserversFromChildAdaptersWhenLastExternalObserverUnregisters() {
        final List<PowerAdapter> childAdapters = ImmutableList.of(
                mock(PowerAdapter.class),
                mock(PowerAdapter.class),
                mock(PowerAdapter.class)
        );
        TreeAdapter treeAdapter = new TreeAdapter(mRootAdapter, new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapters.get(position);
            }
        });
        treeAdapter.setAutoExpand(true);
        DataObserver observer = mock(DataObserver.class);
        treeAdapter.registerDataObserver(observer);
        treeAdapter.unregisterDataObserver(observer);
        for (PowerAdapter adapter : childAdapters) {
            ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
            verify(adapter).registerDataObserver(captor.capture());
            verify(adapter).unregisterDataObserver(eq(captor.getValue()));
        }
    }

    @Test
    public void treeRegistersObserverOnRootAdapterWhenFirstExternalObserverRegisters() {
        PowerAdapter rootAdapter = mock(PowerAdapter.class);
        ChildAdapterSupplier adapterSupplier = mock(ChildAdapterSupplier.class);
        when(adapterSupplier.get(anyInt())).thenReturn(mock(PowerAdapter.class));
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, adapterSupplier);
        treeAdapter.setAutoExpand(true);
        treeAdapter.registerDataObserver(mock(DataObserver.class));
        verify(rootAdapter).registerDataObserver(any(DataObserver.class));
    }

    @Test
    public void treeUnregistersObserverFromRootAdapterWhenLastExternalObserverUnregisters() {
        ChildAdapterSupplier adapterSupplier = mock(ChildAdapterSupplier.class);
        when(adapterSupplier.get(anyInt())).thenReturn(mock(PowerAdapter.class));
        PowerAdapter rootAdapter = mock(PowerAdapter.class);
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, adapterSupplier);
        treeAdapter.setAutoExpand(true);
        DataObserver observer = mock(DataObserver.class);
        treeAdapter.registerDataObserver(observer);
        treeAdapter.unregisterDataObserver(observer);
        ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
        verify(rootAdapter).registerDataObserver(captor.capture());
        verify(rootAdapter).unregisterDataObserver(eq(captor.getValue()));
    }

    @Test
    public void rootNewViewDelegation() {
        Object viewType = mTreeAdapter.getItemViewType(4);
        mTreeAdapter.newView(mParent, viewType);
        verify(mRootAdapter).newView(mParent, viewType);
    }

    @Test
    public void rootBindViewDelegated() {
        mTreeAdapter.bindView(mContainer, mItemView, holder(8));
        verify(mRootAdapter).bindView(any(Container.class), eq(mItemView), argThat(holderWithPosition(2)));
    }

    @Test
    public void rootBindViewHolderPositionUnchangedAfterNonRootInsert() {
        TestHolder holder = new TestHolder(8);
        Holder innerHolder = bindViewAndReturnInnerHolder(mRootAdapter, holder);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
        mChildAdapters.get(0).insert(0, 10);
        holder.setPosition(18);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
    }

    @Test
    public void rootBindViewHolderPositionUpdatedAfterRootInsert() {
        TestHolder holder = new TestHolder(8);
        Holder innerHolder = bindViewAndReturnInnerHolder(mRootAdapter, holder);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
        mRootAdapter.insert(0, 3);
        holder.setPosition(11);
        assertThat(innerHolder.getPosition()).isEqualTo(5);
    }

    @Test
    public void rootBindViewHolderPositionUnchangedAfterNonRootRemove() {
        TestHolder holder = new TestHolder(8);
        Holder innerHolder = bindViewAndReturnInnerHolder(mRootAdapter, holder);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
        mChildAdapters.get(1).remove(0, 2);
        holder.setPosition(6);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
    }

    @Test
    public void rootBindViewHolderPositionUpdatedAfterRootRemove() {
        TestHolder holder = new TestHolder(8);
        Holder innerHolder = bindViewAndReturnInnerHolder(mRootAdapter, holder);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
        mRootAdapter.remove(0, 1);
        holder.setPosition(4);
        assertThat(innerHolder.getPosition()).isEqualTo(1);
    }

    @NonNull
    private Holder bindViewAndReturnInnerHolder(@NonNull PowerAdapter adapter, @NonNull Holder topLevelHolder) {
        mTreeAdapter.bindView(mContainer, mItemView, topLevelHolder);
        ArgumentCaptor<Holder> captor = ArgumentCaptor.forClass(Holder.class);
        verify(adapter).bindView(any(Container.class), eq(mItemView), captor.capture());
        return captor.getValue();
    }

    @Test
    public void rootBindViewContainerItemCountMatchesRootAdapterItemCount() {
        mTreeAdapter.setAllExpanded(true);
        Container innerContainer = bindViewAndReturnInnerContainer(4, mRootAdapter, mock(Container.class));
        assertThat(innerContainer.getItemCount()).isEqualTo(mRootAdapter.getItemCount());
    }

    @Test
    public void rootBindViewContainerScrollToStartScrollsToRootAdapterStart() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(4, mRootAdapter, rootContainer);
        innerContainer.scrollToStart();
        verify(rootContainer).scrollToPosition(0);
    }

    @Test
    public void rootBindViewContainerScrollToEndScrollsToRootAdapterEnd() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(8, mRootAdapter, rootContainer);
        innerContainer.scrollToEnd();
        verify(rootContainer).scrollToPosition(8);
    }

    @Test
    public void rootBindViewContainerScrollToPosition() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(0, mRootAdapter, rootContainer);
        innerContainer.scrollToPosition(1);
        verify(rootContainer).scrollToPosition(4);
    }

    @Test
    public void rootBindViewContainerRootContainerIsActuallyRootContainer() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        when(rootContainer.getRootContainer()).thenReturn(rootContainer);
        Container innerContainer = bindViewAndReturnInnerContainer(4, mRootAdapter, rootContainer);
        assertThat(innerContainer.getRootContainer()).isEqualTo(rootContainer);
    }

    @Test
    public void rootBindViewContainerViewGroupIsRootContainerViewGroup() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        when(rootContainer.getViewGroup()).thenReturn(mContainerViewGroup);
        Container innerContainer = bindViewAndReturnInnerContainer(0, mRootAdapter, rootContainer);
        assertThat(innerContainer.getViewGroup()).isEqualTo(mContainerViewGroup);
    }

    @Test
    public void childBindViewContainerItemCountMatchesChildAdapterItemCount() {
        mTreeAdapter.setAllExpanded(true);
        Container innerContainer = bindViewAndReturnInnerContainer(6, mChildAdapters.get(1), mock(Container.class));
        assertThat(innerContainer.getItemCount()).isEqualTo(mChildAdapters.get(1).getItemCount());
    }

    @Test
    public void childBindViewContainerScrollToStartScrollsToChildAdapterStart() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(11, mChildAdapters.get(2), rootContainer);
        innerContainer.scrollToStart();
        verify(rootContainer).scrollToPosition(9);
    }

    @Test
    public void childBindViewContainerScrollToEndScrollsToChildAdapterEnd() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(7, mChildAdapters.get(1), rootContainer);
        innerContainer.scrollToEnd();
        verify(rootContainer).scrollToPosition(7);
    }

    @Test
    public void childBindViewContainerScrollToPosition() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(9, mChildAdapters.get(2), rootContainer);
        innerContainer.scrollToPosition(1);
        verify(rootContainer).scrollToPosition(10);
    }

    @Test
    public void childBindViewContainerChildRootContainerIsActuallyRootContainer() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        when(rootContainer.getRootContainer()).thenReturn(rootContainer);
        Container innerContainer = bindViewAndReturnInnerContainer(2, mChildAdapters.get(0), rootContainer);
        assertThat(innerContainer.getRootContainer()).isEqualTo(rootContainer);
    }

    @Test
    public void childBindViewContainerViewGroupIsChildContainerViewGroup() {
        mTreeAdapter.setAllExpanded(true);
        Container rootContainer = mock(Container.class);
        when(rootContainer.getViewGroup()).thenReturn(mContainerViewGroup);
        Container innerContainer = bindViewAndReturnInnerContainer(5, mChildAdapters.get(1), rootContainer);
        assertThat(innerContainer.getViewGroup()).isEqualTo(mContainerViewGroup);
    }

    @NonNull
    private Container bindViewAndReturnInnerContainer(int treeAdapterPosition,
                                                      @NonNull PowerAdapter adapter,
                                                      @NonNull Container rootContainer) {
        mTreeAdapter.bindView(rootContainer, mItemView, holder(treeAdapterPosition));
        ArgumentCaptor<Container> captor = ArgumentCaptor.forClass(Container.class);
        verify(adapter).bindView(captor.capture(), eq(mItemView), any(Holder.class));
        return captor.getValue();
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
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapterRef.get();
            }
        });
        treeAdapter.setAutoExpand(true);
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
    public void rootChangeRemovesAndInsertsChildItemsIfExpandedAndChildAdapterInstanceChanges() {
        // TreeAdapter child adapters must change each invocation for this test.
        TreeAdapter treeAdapter = new TreeAdapter(mRootAdapter, new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return new FakeAdapter(3);
            }
        });
        treeAdapter.setAutoExpand(true);
        treeAdapter.registerDataObserver(mock(DataObserver.class));
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

    @Test
    public void rootInsertionPositionAccountsForExpandedChildren() {
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.insert(1, 1);
        verify(observer).onItemRangeInserted(4, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void rootInsertionStateIsCorrect() {
        resetAll();
        mChildAdapters.add(1, spy(new FakeAdapter(3)));
        mRootAdapter.insert(1, 1);
        mChildAdapters.add(1, spy(new FakeAdapter(3)));
        mRootAdapter.insert(1, 1);
        mChildAdapters.add(1, spy(new FakeAdapter(3)));
        mRootAdapter.insert(1, 1);
        verifySubAdapterAllGetCalls()
                .check(mRootAdapter, 0)
                .check(mChildAdapters.get(0), 0)
                .check(mChildAdapters.get(0), 1)
                .check(mChildAdapters.get(0), 2)
                .check(mRootAdapter, 1)
                .check(mRootAdapter, 2)
                .check(mRootAdapter, 3)
                .check(mRootAdapter, 4)
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

    @Test
    public void rootRemovalItemCountIsConsistent() {
        final FakeAdapter childAdapter = new FakeAdapter(10);
        FakeAdapter rootAdapter = new FakeAdapter(1);
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(treeAdapter);
        treeAdapter.registerDataObserver(verifyingObserver);
        rootAdapter.clear();
        assertThat(treeAdapter.getItemCount()).isEqualTo(0);
        verifyingObserver.assertItemCountConsistent();
    }

    @Ignore
    @Test
    public void rootRemovalRemovesSavedState() {
        // TODO: Check that a root removal removes the saved state entry for that root ID.
    }


    @Test
    public void rootMoveIsTranslatedAndIncludesExpandedChildren() {
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.move(1, 2, 1);
        verify(observer).onItemRangeMoved(4, 8, 4);
        verifySubAdapterAllGetCalls()
                .checkRange(mRootAdapter, 0, 1)
                .checkRange(mChildAdapters.get(0), 0, 3)
                .checkRange(mRootAdapter, 1, 1)
                .checkRange(mChildAdapters.get(2), 0, 3)
                .checkRange(mRootAdapter, 2, 1)
                .checkRange(mChildAdapters.get(1), 0, 3)
                .verify(mTreeAdapter);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void rootMoveIsTranslatedAndIncludesExpandedChildren2() {
        DataObserver observer = registerMockDataObserver();
        mRootAdapter.move(0, 1, 2);
        verify(observer).onItemRangeMoved(0, 4, 8);
        verifySubAdapterAllGetCalls()
                .checkRange(mRootAdapter, 0, 1)
                .checkRange(mChildAdapters.get(2), 0, 3)
                .checkRange(mRootAdapter, 1, 1)
                .checkRange(mChildAdapters.get(0), 0, 3)
                .checkRange(mRootAdapter, 2, 1)
                .checkRange(mChildAdapters.get(1), 0, 3)
                .verify(mTreeAdapter);
        verifyNoMoreInteractions(observer);
    }

    // TODO: After every type of root change, check that get*() calls map correctly.

    @Test
    public void childNewViewDelegated() {
        Object viewType = mTreeAdapter.getItemViewType(3);
        mTreeAdapter.newView(mParent, viewType);
        verify(mChildAdapters.get(0)).newView(mParent, viewType);
        verifyNewViewNeverCalled(mChildAdapters.get(1));
        verifyNewViewNeverCalled(mChildAdapters.get(2));
    }

    @Test
    public void childBindViewDelegated() {
        mTreeAdapter.bindView(mContainer, mItemView, holder(10));
        verifyBindViewNeverCalled(mChildAdapters.get(0));
        verifyBindViewNeverCalled(mChildAdapters.get(1));
        verify(mChildAdapters.get(2)).bindView(any(Container.class), eq(mItemView), argThat(holderWithPosition(1)));
    }

    @Test
    public void childInsertionGeneratesTreeInsertion() {
        final FakeAdapter childAdapter = new FakeAdapter(3);
        TreeAdapter treeAdapter = new TreeAdapter(new FakeAdapter(1), new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        treeAdapter.setAutoExpand(true);
        treeAdapter.registerDataObserver(mock(DataObserver.class));
        DataObserver observer = mock(DataObserver.class);
        treeAdapter.registerDataObserver(observer);
        childAdapter.append(2);
        verify(observer).onItemRangeInserted(4, 2);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childRemovalGeneratesTreeRemoval() {
        final FakeAdapter childAdapter = new FakeAdapter(3);
        TreeAdapter treeAdapter = new TreeAdapter(new FakeAdapter(1), new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapter;
            }
        });
        treeAdapter.setAutoExpand(true);
        treeAdapter.registerDataObserver(mock(DataObserver.class));
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

    @Test
    public void childMoveIsTranslated() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).move(0, 2, 1);
        verify(observer).onItemRangeMoved(9, 11, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childMoveIsTranslated2() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(1).move(0, 1, 2);
        verify(observer).onItemRangeMoved(5, 6, 2);
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
    public void restoreStateWorksWithoutObserverRegistered() {
        final List<FakeAdapter> childAdapters = newArrayList(
                new FakeAdapter(3),
                new FakeAdapter(3),
                new FakeAdapter(3)
        );
        FakeLongAdapter rootAdapter = new FakeLongAdapter();
        Collections.addAll(rootAdapter, 1L, 3L, 5L);
        ChildAdapterSupplier childAdapterSupplier = new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapters.get(position);
            }
        };
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, childAdapterSupplier);
        treeAdapter.setExpanded(0, true);
        treeAdapter.setExpanded(1, false);
        treeAdapter.setExpanded(2, true);
        assertThat(treeAdapter.isExpanded(0)).isTrue();
        assertThat(treeAdapter.isExpanded(1)).isFalse();
        assertThat(treeAdapter.isExpanded(2)).isTrue();
        Parcelable state = treeAdapter.saveInstanceState();
        TreeAdapter treeAdapter2 = new TreeAdapter(rootAdapter, childAdapterSupplier);
        treeAdapter2.restoreInstanceState(state);
        assertThat(treeAdapter2.isExpanded(0)).isTrue();
        assertThat(treeAdapter2.isExpanded(1)).isFalse();
        assertThat(treeAdapter2.isExpanded(2)).isTrue();
    }

    @Test
    public void restoreStateWorksWithObserverRegistered() {
        final List<FakeAdapter> childAdapters = newArrayList(
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(3))
        );
        FakeLongAdapter rootAdapter = spy(new FakeLongAdapter());
        Collections.addAll(rootAdapter, 1L, 3L, 5L);
        ChildAdapterSupplier childAdapterSupplier = new ChildAdapterSupplier() {
            @NonNull
            @Override
            public PowerAdapter get(int position) {
                return childAdapters.get(position);
            }
        };
        TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, childAdapterSupplier);
        treeAdapter.setExpanded(0, true);
        treeAdapter.setExpanded(1, false);
        treeAdapter.setExpanded(2, true);
        assertThat(treeAdapter.isExpanded(0)).isTrue();
        assertThat(treeAdapter.isExpanded(1)).isFalse();
        assertThat(treeAdapter.isExpanded(2)).isTrue();
        Parcelable state = treeAdapter.saveInstanceState();
        TreeAdapter treeAdapter2 = new TreeAdapter(rootAdapter, childAdapterSupplier);
        treeAdapter2.registerDataObserver(mock(DataObserver.class));
        treeAdapter2.restoreInstanceState(state);
        assertThat(treeAdapter2.isExpanded(0)).isTrue();
        assertThat(treeAdapter2.isExpanded(1)).isFalse();
        assertThat(treeAdapter2.isExpanded(2)).isTrue();
        resetAll();
        verifySubAdapterAllGetCalls()
                .check(rootAdapter, 0)
                .checkRange(childAdapters.get(0), 0, 3)
                .check(rootAdapter, 1)
                .check(rootAdapter, 2)
                .checkRange(childAdapters.get(2), 0, 3)
                .verify(treeAdapter2);
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
