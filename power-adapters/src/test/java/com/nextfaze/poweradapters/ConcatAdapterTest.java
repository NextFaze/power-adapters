package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.common.collect.ImmutableList;
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

    @Before
    public void setUp() throws Exception {
        mChildAdapters = newArrayList(
                spy(new FakeAdapter(3)),
                spy(new FakeAdapter(4)),
                spy(new FakeAdapter(5))
        );
        mConcatAdapter = new ConcatAdapter.Builder().addAll(mChildAdapters).build();
        mConcatAdapter.registerDataObserver(mObserver);
        mConcatAdapter.registerDataObserver(new VerifyingObserver(mConcatAdapter));
        for (PowerAdapter adapter : mChildAdapters) {
            verify(adapter).registerDataObserver(any(DataObserver.class));
            verify(adapter).onFirstObserverRegistered();
        }
        mParent = new FrameLayout(RuntimeEnvironment.application);
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
        PowerAdapter concatAdapter = new ConcatAdapter.Builder().add(childAdapter).build();
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
        PowerAdapter concatAdapter = new ConcatAdapter.Builder().addAll(childAdapters).build();
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
        ViewType viewType = mConcatAdapter.getItemViewType(4);
        mConcatAdapter.newView(mParent, viewType);
        verifyNewViewNeverCalled(mChildAdapters.get(0));
        verify(mChildAdapters.get(1)).newView(mParent, viewType);
        verifyNewViewNeverCalled(mChildAdapters.get(2));
    }

    @Test
    public void bindViewDelegatedToChild() {
        mConcatAdapter.bindView(mItemView, holder(8));
        verifyBindViewNeverCalled(mChildAdapters.get(0));
        verifyBindViewNeverCalled(mChildAdapters.get(1));
        verify(mChildAdapters.get(2)).bindView(eq(mItemView), argThat(holderWithPosition(1)));
    }

    @Test
    public void childChange() {
        mChildAdapters.get(1).change(1, 3);
        verify(mObserver).onItemRangeChanged(4, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childInsert() {
        mChildAdapters.get(2).insert(3, 2);
        verifySubAdapterAllGetCalls()
                .checkRange(mChildAdapters.get(0), 0, 3)
                .checkRange(mChildAdapters.get(1), 0, 4)
                .checkRange(mChildAdapters.get(2), 0, 7)
                .verify(mConcatAdapter);
        verify(mObserver).onItemRangeInserted(10, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childRemove() {
        mChildAdapters.get(0).remove(0, 3);
        verifySubAdapterAllGetCalls()
                .checkRange(mChildAdapters.get(0), 0, 0)
                .checkRange(mChildAdapters.get(1), 0, 4)
                .checkRange(mChildAdapters.get(2), 0, 5)
                .verify(mConcatAdapter);
        verify(mObserver).onItemRangeRemoved(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childMove() {
        mChildAdapters.get(2).move(1, 3, 2);
        verifySubAdapterAllGetCalls()
                .checkRange(mChildAdapters.get(0), 0, 3)
                .checkRange(mChildAdapters.get(1), 0, 4)
                .checkRange(mChildAdapters.get(2), 0, 5)
                .verify(mConcatAdapter);
        verify(mObserver).onItemRangeMoved(8, 10, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void childState1() {
        List<FakeAdapter> childAdapters = newArrayList(
                spy(new FakeAdapter(1)),
                spy(new FakeAdapter(0)),
                spy(new FakeAdapter(1))
        );
        PowerAdapter concatAdapter = new ConcatAdapter.Builder().addAll(childAdapters).build();
        concatAdapter.registerDataObserver(mObserver);
        for (PowerAdapter adapter : childAdapters) {
            verify(adapter).registerDataObserver(any(DataObserver.class));
            verify(adapter).onFirstObserverRegistered();
        }
        verifySubAdapterAllGetCalls()
                .checkRange(childAdapters.get(0), 0, 1)
                .checkRange(childAdapters.get(2), 0, 1)
                .verify(concatAdapter);
        verifyNoMoreInteractions(mObserver);
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
        PowerAdapter concatAdapter = new ConcatAdapter.Builder().addAll(childAdapters).build();
        concatAdapter.registerDataObserver(mObserver);
        for (PowerAdapter adapter : childAdapters) {
            verify(adapter).registerDataObserver(any(DataObserver.class));
            verify(adapter).onFirstObserverRegistered();
        }
        verifySubAdapterAllGetCalls()
                .checkRange(childAdapters.get(0), 0, 12)
                .checkRange(childAdapters.get(1), 0, 6)
                .checkRange(childAdapters.get(4), 0, 11)
                .verify(concatAdapter);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void itemCountConsistentWhenChildAdaptersAreDependent() {
        FakeAdapter fakeAdapter = spy(new FakeAdapter(3));
        PowerAdapter concatAdapter = new ConcatAdapter.Builder().addAll(fakeAdapter, fakeAdapter).build();
        DataObserver observer = mock(DataObserver.class);
        concatAdapter.registerDataObserver(observer);
        concatAdapter.registerDataObserver(new VerifyingObserver(concatAdapter));
        fakeAdapter.insert(0, 1);
        verifySubAdapterAllGetCalls()
                .checkRange(fakeAdapter, 0, 4)
                .checkRange(fakeAdapter, 0, 4)
                .verify(concatAdapter);
        verify(observer).onItemRangeInserted(3, 1); // Could be (0, 1), (4, 1) also
        verify(observer).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(observer);
    }
}