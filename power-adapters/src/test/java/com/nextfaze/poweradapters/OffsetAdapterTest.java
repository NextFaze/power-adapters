package com.nextfaze.poweradapters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class OffsetAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private FakeAdapter mFakeAdapter;
    private OffsetAdapter mOffsetAdapter;

    @Before
    public void setUp() throws Exception {
        configure(5, 10);
    }

    private void configure(int offset, int count) {
        mFakeAdapter = spy(new FakeAdapter(count));
        mOffsetAdapter = new OffsetAdapter(mFakeAdapter, offset);
        mOffsetAdapter.registerDataObserver(mObserver);
        mOffsetAdapter.registerDataObserver(new VerifyingAdapterObserver(mOffsetAdapter));
        verify(mFakeAdapter).onFirstObserverRegistered();
    }

    @Test
    public void negativeOffsetClamped() {
        assertThat(new OffsetAdapter(mFakeAdapter, -10).getOffset()).isEqualTo(0);
    }

    @Test
    public void reducedSize() {
        assertThat(mOffsetAdapter.getItemCount()).isEqualTo(5);
    }

    @Test
    public void clippedContents() {
        verifyState(5, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getItemViewTypeOutOfBoundsThrows() {
        mOffsetAdapter.getItemViewType(7);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getItemIdOutOfBoundsThrows() {
        mOffsetAdapter.getItemId(5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isEnabledOutOfBoundsThrows() {
        mOffsetAdapter.isEnabled(6);
    }

    @Test
    public void changeOutOfBounds() {
        mFakeAdapter.change(2, 1);
        verifyState(5, 5);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void changeWithinBounds() {
        mFakeAdapter.change(6, 3);
        verifyState(5, 5);
        verify(mObserver).onItemRangeChanged(1, 3);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void changeBoundaryStraddling() {
        mFakeAdapter.change(3, 3);
        verifyState(5, 5);
        verify(mObserver).onItemRangeChanged(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertOutOfBounds() {
        mFakeAdapter.insert(1, 1);
        verifyState(5, 6);
        verify(mObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertOutOfBounds2() {
        mFakeAdapter.insert(0, 5);
        verifyState(5, 10);
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling() {
        configure(5, 10);
        mFakeAdapter.insert(3, 3);
        verifyState(5, 8);
        verify(mObserver).onItemRangeInserted(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling2() {
        configure(5, 10);
        mFakeAdapter.insert(4, 10);
        verifyState(5, 15);
        verify(mObserver).onItemRangeInserted(0, 10);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmpty() {
        configure(5, 0);
        mFakeAdapter.insert(0, 10);
        verifyState(5, 5);
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromBelowOffset() {
        configure(5, 3);
        mFakeAdapter.insert(1, 10);
        verifyState(5, 8);
        verify(mObserver).onItemRangeInserted(0, 8);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromBelowOffset2() {
        configure(10, 5);
        mFakeAdapter.insert(3, 20);
        verifyState(10, 15);
        verify(mObserver).onItemRangeInserted(0, 15);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertWithinBounds() {
        configure(5, 10);
        mFakeAdapter.insert(6, 3);
        verifyState(5, 8);
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertWithinBounds2() {
        configure(5, 10);
        mFakeAdapter.insert(10, 10);
        verifyState(5, 15);
        verify(mObserver).onItemRangeInserted(5, 10);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeOutOfBounds() {
        configure(5, 10);
        mFakeAdapter.remove(2, 1);
        verifyState(5, 4);
        verify(mObserver).onItemRangeRemoved(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeOutOfBounds2() {
        configure(5, 10);
        mFakeAdapter.remove(0, 5);
        assertEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeOutOfBounds3() {
        configure(5, 10);
        mFakeAdapter.remove(0, 4);
        verifyState(5, 1);
        verify(mObserver).onItemRangeRemoved(0, 4);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeWithinBounds() {
        configure(5, 10);
        mFakeAdapter.remove(7, 2);
        verifyState(5, 3);
        verify(mObserver).onItemRangeRemoved(2, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeWithinBounds2() {
        configure(5, 10);
        mFakeAdapter.remove(5, 5);
        assertEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeWithinBounds3() {
        configure(5, 10);
        mFakeAdapter.remove(9, 1);
        verifyState(5, 4);
        verify(mObserver).onItemRangeRemoved(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddling() {
        configure(5, 10);
        mFakeAdapter.remove(3, 4);
        verifyState(5, 1);
        verify(mObserver).onItemRangeRemoved(0, 4);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddling2() {
        configure(5, 10);
        mFakeAdapter.remove(4, 5);
        assertEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddling3() {
        configure(5, 10);
        mFakeAdapter.remove(1, 8);
        assertEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeAll() {
        mFakeAdapter.clear();
        assertEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void moveNotifiesOfChange() {
        mFakeAdapter.move(1, 3, 2);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    private void verifyState(int start, int count) {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, start, count)
                .verify(mOffsetAdapter);
    }

    private void assertEmpty() {
        assertThat(mOffsetAdapter.getItemCount()).isEqualTo(0);
    }
}
