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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static com.nextfaze.poweradapters.AdapterTestUtils.*;
import static com.nextfaze.poweradapters.AdapterVerifier.verifySubAdapterAllGetCalls;
import static com.nextfaze.poweradapters.ArgumentMatchers.holderWithPosition;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class ConcatAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private List<FakeAdapter> mChildAdapters;
    private PowerAdapter mConcatAdapter;
    private ViewGroup mParent;
    private View mItemView;
    private ViewGroup mContainerViewGroup;

    @Mock
    private Container mContainer;

    @Before
    public void setUp() throws Exception {
        mChildAdapters = newArrayList(
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(4)),
                spy(new FakeAdapter(5))
        );
        mConcatAdapter = new ConcatAdapterBuilder().addAll(mChildAdapters).build();
        mConcatAdapter.registerDataObserver(mObserver);
        mConcatAdapter.registerDataObserver(new VerifyingAdapterObserver(mConcatAdapter));
        for (PowerAdapter adapter : mChildAdapters) {
            verify(adapter).registerDataObserver(any(DataObserver.class));
            verify(adapter).onFirstObserverRegistered();
        }
        mParent = new FrameLayout(RuntimeEnvironment.application);
        mContainerViewGroup = new FrameLayout(RuntimeEnvironment.application);
        mItemView = new View(RuntimeEnvironment.application);
    }

    @Test
    public void getCallsAreTranslatedToChildren() {
        verifySubAdapterAllGetCalls()
                .checkRange(mChildAdapters.get(0), 0, 3)
                .checkRange(mChildAdapters.get(1), 0, 4)
                .checkRange(mChildAdapters.get(2), 0, 5)
                .verify(mConcatAdapter);
    }

    @Test
    public void parentItemCountIsSumOfChildAdapters() {
        assertThat(mConcatAdapter.getItemCount()).isEqualTo(12);
    }

    @Test
    public void parentRegistersObserversOnChildAdaptersWhenFirstExternalObserverRegisters() {
        PowerAdapter childAdapter = mock(PowerAdapter.class);
        PowerAdapter concatAdapter = new ConcatAdapterBuilder().add(childAdapter).build();
        concatAdapter.registerDataObserver(mock(DataObserver.class));
        verify(childAdapter, times(1)).registerDataObserver(any(DataObserver.class));
    }

    @Test
    public void parentUnregistersObserversFromChildAdaptersWhenLastExternalObserverUnregisters() {
        List<PowerAdapter> childAdapters = ImmutableList.of(
                mock(PowerAdapter.class),
                mock(PowerAdapter.class),
                mock(PowerAdapter.class)
        );
        PowerAdapter concatAdapter = new ConcatAdapterBuilder().addAll(childAdapters).build();
        concatAdapter.registerDataObserver(mObserver);
        concatAdapter.unregisterDataObserver(mObserver);
        for (PowerAdapter adapter : childAdapters) {
            ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
            verify(adapter).registerDataObserver(captor.capture());
            verify(adapter).unregisterDataObserver(eq(captor.getValue()));
        }
    }

    @Test
    public void newViewDelegatedToChild() {
        Object viewType = mConcatAdapter.getItemViewType(4);
        mConcatAdapter.newView(mParent, viewType);
        verifyNewViewNeverCalled(mChildAdapters.get(0));
        verify(mChildAdapters.get(1)).newView(mParent, viewType);
        verifyNewViewNeverCalled(mChildAdapters.get(2));
    }

    @Test
    public void childBindViewDelegated() {
        mConcatAdapter.bindView(mContainer, mItemView, holder(8));
        verifyBindViewNeverCalled(mChildAdapters.get(0));
        verifyBindViewNeverCalled(mChildAdapters.get(1));
        verify(mChildAdapters.get(2)).bindView(any(Container.class), eq(mItemView), argThat(holderWithPosition(1)));
    }

    @Test
    public void childBindViewHolderPositionUnchangedAfterDifferentChildInsert() {
        TestHolder holder = new TestHolder(8);
        Holder innerHolder = bindViewAndReturnInnerHolder(mChildAdapters.get(2), holder);
        assertThat(innerHolder.getPosition()).isEqualTo(1);
        mChildAdapters.get(0).insert(0, 10);
        holder.setPosition(18);
        assertThat(innerHolder.getPosition()).isEqualTo(1);
    }

    @Test
    public void childBindViewHolderPositionUpdatedAfterSameChildInsert() {
        TestHolder holder = new TestHolder(8);
        Holder innerHolder = bindViewAndReturnInnerHolder(mChildAdapters.get(2), holder);
        assertThat(innerHolder.getPosition()).isEqualTo(1);
        mChildAdapters.get(2).insert(0, 3);
        holder.setPosition(11);
        assertThat(innerHolder.getPosition()).isEqualTo(4);
    }

    @Test
    public void childBindViewHolderPositionUnchangedAfterDifferentChildRemove() {
        TestHolder holder = new TestHolder(5);
        Holder innerHolder = bindViewAndReturnInnerHolder(mChildAdapters.get(1), holder);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
        mChildAdapters.get(0).remove(1, 1);
        holder.setPosition(4);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
    }

    @Test
    public void childBindViewHolderPositionUpdatedAfterSameChildRemove() {
        TestHolder holder = new TestHolder(5);
        Holder innerHolder = bindViewAndReturnInnerHolder(mChildAdapters.get(1), holder);
        assertThat(innerHolder.getPosition()).isEqualTo(2);
        mChildAdapters.get(1).remove(0, 1);
        holder.setPosition(4);
        assertThat(innerHolder.getPosition()).isEqualTo(1);
    }

    @NonNull
    private Holder bindViewAndReturnInnerHolder(@NonNull PowerAdapter adapter, @NonNull Holder topLevelHolder) {
        mConcatAdapter.bindView(mContainer, mItemView, topLevelHolder);
        ArgumentCaptor<Holder> captor = ArgumentCaptor.forClass(Holder.class);
        verify(adapter).bindView(any(Container.class), eq(mItemView), captor.capture());
        return captor.getValue();
    }

    @Test
    public void childChange() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(1).change(1, 3);
        verify(observer).onItemRangeChanged(4, 3);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childInsert() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).insert(3, 2);
        verifySubAdapterAllGetCalls()
                .checkRange(mChildAdapters.get(0), 0, 3)
                .checkRange(mChildAdapters.get(1), 0, 4)
                .checkRange(mChildAdapters.get(2), 0, 7)
                .verify(mConcatAdapter);
        verify(observer).onItemRangeInserted(10, 2);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childRemove() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(0).remove(0, 3);
        verifySubAdapterAllGetCalls()
                .checkRange(mChildAdapters.get(0), 0, 0)
                .checkRange(mChildAdapters.get(1), 0, 4)
                .checkRange(mChildAdapters.get(2), 0, 5)
                .verify(mConcatAdapter);
        verify(observer).onItemRangeRemoved(0, 3);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childMove() {
        DataObserver observer = registerMockDataObserver();
        mChildAdapters.get(2).move(1, 3, 2);
        verifySubAdapterAllGetCalls()
                .checkRange(mChildAdapters.get(0), 0, 3)
                .checkRange(mChildAdapters.get(1), 0, 4)
                .checkRange(mChildAdapters.get(2), 0, 5)
                .verify(mConcatAdapter);
        verify(observer).onItemRangeMoved(8, 10, 2);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void childState1() {
        List<FakeAdapter> childAdapters = newArrayList(
                spy(new FakeAdapter(1)),
                spy(new FakeAdapter(0)),
                spy(new FakeAdapter(1))
        );
        PowerAdapter concatAdapter = new ConcatAdapterBuilder().addAll(childAdapters).build();
        concatAdapter.registerDataObserver(mock(DataObserver.class));
        for (PowerAdapter adapter : childAdapters) {
            verify(adapter).registerDataObserver(any(DataObserver.class));
            verify(adapter).onFirstObserverRegistered();
        }
        verifySubAdapterAllGetCalls()
                .checkRange(childAdapters.get(0), 0, 1)
                .checkRange(childAdapters.get(2), 0, 1)
                .verify(concatAdapter);
    }

    @Test
    public void childState2() {
        List<FakeAdapter> childAdapters = newArrayList(
                spy(new FakeAdapter(12)),
                spy(new FakeAdapter(6)),
                spy(new FakeAdapter(0)),
                spy(new FakeAdapter(0)),
                spy(new FakeAdapter(11))
        );
        PowerAdapter concatAdapter = new ConcatAdapterBuilder().addAll(childAdapters).build();
        concatAdapter.registerDataObserver(mock(DataObserver.class));
        for (PowerAdapter adapter : childAdapters) {
            verify(adapter).registerDataObserver(any(DataObserver.class));
            verify(adapter).onFirstObserverRegistered();
        }
        verifySubAdapterAllGetCalls()
                .checkRange(childAdapters.get(0), 0, 12)
                .checkRange(childAdapters.get(1), 0, 6)
                .checkRange(childAdapters.get(4), 0, 11)
                .verify(concatAdapter);
    }

    @Test
    public void itemCountConsistentWhenChildAdaptersAreDependent() {
        FakeAdapter fakeAdapter = spy(new FakeAdapter(3));
        PowerAdapter concatAdapter = concat(fakeAdapter, fakeAdapter);
        DataObserver observer = mock(DataObserver.class);
        concatAdapter.registerDataObserver(observer);
        concatAdapter.registerDataObserver(new VerifyingAdapterObserver(concatAdapter));
        fakeAdapter.insert(0, 1);
        verifySubAdapterAllGetCalls()
                .checkRange(fakeAdapter, 0, 4)
                .checkRange(fakeAdapter, 0, 4)
                .verify(concatAdapter);
        verify(observer).onItemRangeInserted(0, 6);
        verify(observer).onItemRangeInserted(3, 1); // TODO: Could be (0, 1), (4, 1) also.
        verify(observer).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void itemCountConsistentWhenChildAdaptersAreDependent2() {
        PowerAdapter adapter = new FakeAdapter(3);
        PowerAdapter dependentAdapter = new FakeAdapter(1).showOnlyWhile(Condition.adapter(adapter, new Predicate<PowerAdapter>() {
            @Override
            public boolean apply(PowerAdapter adapter) {
                return adapter.getItemCount() > 0;
            }
        }));
        PowerAdapter concatAdapter = concat(dependentAdapter, adapter);
        concatAdapter.registerDataObserver(new VerifyingAdapterObserver(concatAdapter));
    }

    @Test
    public void itemCount() {
        FakeAdapter adapter = new FakeAdapter(3);
        PowerAdapter concatAdapter = concat(adapter, PowerAdapter.EMPTY);
        concatAdapter.registerDataObserver(new VerifyingAdapterObserver(concatAdapter));
        adapter.clear();
    }

    @Test
    public void childBindViewContainerItemCountMatchesChildAdapterItemCount() {
        Container innerContainer = bindViewAndReturnInnerContainer(5, mChildAdapters.get(1), mock(Container.class));
        assertThat(innerContainer.getItemCount()).isEqualTo(mChildAdapters.get(1).getItemCount());
    }

    @Test
    public void childBindViewContainerScrollToStartScrollsToChildAdapterStart() {
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(4, mChildAdapters.get(1), rootContainer);
        innerContainer.scrollToStart();
        verify(rootContainer).scrollToPosition(3);
    }

    @Test
    public void childBindViewContainerScrollToEndScrollsToChildAdapterEnd() {
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(9, mChildAdapters.get(2), rootContainer);
        innerContainer.scrollToEnd();
        verify(rootContainer).scrollToPosition(11);
    }

    @Test
    public void childBindViewContainerScrollToPosition() {
        Container rootContainer = mock(Container.class);
        Container innerContainer = bindViewAndReturnInnerContainer(9, mChildAdapters.get(2), rootContainer);
        innerContainer.scrollToPosition(1);
        verify(rootContainer).scrollToPosition(8);
    }

    @Test
    public void childBindViewContainerChildRootContainerIsActuallyRootContainer() {
        Container rootContainer = mock(Container.class);
        when(rootContainer.getRootContainer()).thenReturn(rootContainer);
        Container innerContainer = bindViewAndReturnInnerContainer(5, mChildAdapters.get(1), rootContainer);
        assertThat(innerContainer.getRootContainer()).isEqualTo(rootContainer);
    }

    @Test
    public void childBindViewContainerViewGroupIsChildContainerViewGroup() {
        Container rootContainer = mock(Container.class);
        when(rootContainer.getViewGroup()).thenReturn(mContainerViewGroup);
        Container innerContainer = bindViewAndReturnInnerContainer(7, mChildAdapters.get(2), rootContainer);
        assertThat(innerContainer.getViewGroup()).isEqualTo(mContainerViewGroup);
    }

    @NonNull
    private Container bindViewAndReturnInnerContainer(int concatAdapterPosition,
                                                      @NonNull PowerAdapter adapter,
                                                      @NonNull Container rootContainer) {
        mConcatAdapter.bindView(rootContainer, mItemView, holder(concatAdapterPosition));
        ArgumentCaptor<Container> captor = ArgumentCaptor.forClass(Container.class);
        verify(adapter).bindView(captor.capture(), eq(mItemView), any(Holder.class));
        return captor.getValue();
    }

    @NonNull
    private DataObserver registerMockDataObserver() {
        DataObserver observer = mock(DataObserver.class);
        mConcatAdapter.registerDataObserver(observer);
        return observer;
    }

    @NonNull
    private static PowerAdapter concat(@NonNull PowerAdapter... adapters) {
        return new ConcatAdapterBuilder().addAll(adapters).build();
    }
}