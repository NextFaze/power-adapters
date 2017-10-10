package com.nextfaze.poweradapters;

import com.nextfaze.poweradapters.test.FakeAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class LimitAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private FakeAdapter mFakeAdapter;
    private LimitAdapter mLimitAdapter;
    private VerifyingAdapterObserver mVerifyingObserver;

    @Before
    public void setUp() throws Exception {
        configure(5, 10);
    }

    @After
    public void tearDown() throws Exception {
        mVerifyingObserver.assertItemCountConsistent();
    }

    private void configure(int limit, int count) {
        mFakeAdapter = spy(new FakeAdapter(count));
        mLimitAdapter = new LimitAdapter(mFakeAdapter, limit);
        mLimitAdapter.registerDataObserver(mObserver);
        mVerifyingObserver = new VerifyingAdapterObserver(mLimitAdapter);
        mLimitAdapter.registerDataObserver(mVerifyingObserver);
        verify(mFakeAdapter).onFirstObserverRegistered();
    }

    @Test
    public void negativeLimitClamped() {
        assertThat(new LimitAdapter(mFakeAdapter, -50).getLimit()).isEqualTo(0);
    }

    @Test
    public void limitedSize() {
        assertThat(mLimitAdapter.getItemCount()).isEqualTo(5);
    }

    @Test
    public void limitedContents() {
        verifyState(5);
    }

    @Test
    public void setLimitNotifiesOfInsertion() {
        mLimitAdapter.setLimit(10);
        verify(mObserver).onItemRangeInserted(5, 5);
    }

    @Test
    public void setLimitNotifiesOfRemoval() {
        mLimitAdapter.setLimit(0);
        verify(mObserver).onItemRangeRemoved(0, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getItemViewTypeOutOfBoundsThrows() {
        mLimitAdapter.getItemViewType(7);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getItemIdOutOfBoundsThrows() {
        mLimitAdapter.getItemId(5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void isEnabledOutOfBoundsThrows() {
        mLimitAdapter.isEnabled(6);
    }

    @Test
    public void changeOutOfBounds() {
        mFakeAdapter.change(5, 1);
        verifyState(5);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void changeNormal() {
        mFakeAdapter.change(0, 3);
        verifyState(5);
        verify(mObserver).onItemRangeChanged(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void changeBoundaryStraddlingClipped() {
        mFakeAdapter.change(3, 3);
        verifyState(5);
        verify(mObserver).onItemRangeChanged(3, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertOutOfBounds() {
        mFakeAdapter.append(1);
        verifyState(5);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling() {
        configure(5, 3);
        mFakeAdapter.insert(1, 3);
        verifyState(5);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling2() {
        configure(5, 10);
        mFakeAdapter.insert(0, 4);
        verifyState(5);
        verify(mObserver).onItemRangeChanged(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling3() {
        configure(5, 10);
        mFakeAdapter.insert(4, 2);
        verifyState(5);
        verify(mObserver).onItemRangeChanged(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddlingNonEmptyInitially() {
        configure(3, 1);
        mFakeAdapter.insert(0, 6);
        verifyState(3);
        verify(mObserver).onItemRangeRemoved(0, 1);
        verify(mObserver).onItemRangeInserted(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddlingNonEmptyInitially2() {
        configure(4, 2);
        mFakeAdapter.insert(1, 6);
        verifyState(4);
        verify(mObserver).onItemRangeRemoved(1, 1);
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddlingNonEmptyInitially3() {
        configure(7, 6);
        mFakeAdapter.insert(3, 6);
        verifyState(7);
        verify(mObserver).onItemRangeRemoved(3, 3);
        verify(mObserver).onItemRangeInserted(3, 4);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFullRangeFromStart() {
        configure(5, 5);
        mFakeAdapter.insert(0, 5);
        verifyState(5);
        verify(mObserver).onItemRangeChanged(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertAppend() {
        configure(5, 2);
        mFakeAdapter.append(2);
        verifyState(4);
        verify(mObserver).onItemRangeInserted(2, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmpty() {
        configure(5, 0);
        mFakeAdapter.append(2);
        verifyState(2);
        verify(mObserver).onItemRangeInserted(0, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeOutOfBoundsDropped() {
        mFakeAdapter.remove(5, 1);
        verifyState(5);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void removeAll() {
        configure(5, 8);
        mFakeAdapter.clear();
        assertThat(mLimitAdapter.getItemCount()).isEqualTo(0);
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddlingBrokenIntoRemoveAndInsert() {
        configure(5, 9);
        mFakeAdapter.remove(2, 5);
        verifyState(4);
        verify(mObserver).onItemRangeRemoved(2, 3);
        verify(mObserver).onItemRangeInserted(2, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddlingBrokenIntoRemoveAndInsert2() {
        configure(5, 9);
        mFakeAdapter.remove(1, 5);
        verifyState(4);
        verify(mObserver).onItemRangeRemoved(1, 4);
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeFirstTwoFromLargeInnerRangeOnlyIssuesChange() {
        configure(5, 10);
        mFakeAdapter.remove(0, 2);
        verifyState(5);
        verify(mObserver).onItemRangeChanged(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeSingleFromEnd() {
        configure(5, 5);
        mFakeAdapter.remove(4, 1);
        verifyState(4);
        verify(mObserver).onItemRangeRemoved(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeSingleFromMiddle() {
        configure(5, 5);
        mFakeAdapter.remove(2, 1);
        verifyState(4);
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Ignore
    @Test
    public void moveOutOfBoundsDropped() {
        // TODO: Check that a move that's entirely out of bounds results in no notification.
        throw new UnsupportedOperationException();
    }

    @Test
    public void moveNotifiesOfChange() {
        mFakeAdapter.move(0, 1, 1);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    private void verifyState(int count) {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, 0, count)
                .verify(mLimitAdapter);
    }
}
