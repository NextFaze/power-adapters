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
import static com.nextfaze.poweradapters.AdapterTestUtils.fakeIntAdapter;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class OffsetAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mDataObserver;

    private FakeAdapter<Integer> mFakeAdapter;
    private OffsetAdapter mOffsetAdapter;

    @Before
    public void setUp() throws Exception {
        mFakeAdapter = spy(fakeIntAdapter(10));
        mOffsetAdapter = new OffsetAdapter(mFakeAdapter, 5);
        mOffsetAdapter.registerDataObserver(mDataObserver);
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
        verifyExpectedOffsetState();
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
    public void outOfBoundsChangeDropped() {
        mFakeAdapter.set(2, 1);
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsChangeStateUnchanged() {
        mFakeAdapter.set(4, 1);
        verifyExpectedOffsetState();
    }

    @Test
    public void outOfBoundsInsertDropped() {
        mFakeAdapter.add(1, 9);
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsInsertStateUnchanged() {
        mFakeAdapter.add(1, 9);
        verifyExpectedOffsetState();
    }

    @Test
    public void outOfBoundsRemoveDropped() {
        mFakeAdapter.remove(2);
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsRemoveStateUnchanged() {
        mFakeAdapter.remove(2);
        verifyExpectedOffsetState();
    }

    @Test
    public void outOfBoundsMoveDropped() {
        // TODO: Check that a move that's entirely out of bounds results in no notification.
        throw new UnsupportedOperationException();
    }

    @Test
    public void boundaryStraddlingChangeClipped() {
        mFakeAdapter.set(3, 0, 0, 0);
        verify(mDataObserver).onItemRangeChanged(0, 1);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, 5, 5)
                .verify(mOffsetAdapter);
        verifyNoMoreInteractions(mDataObserver);
    }

    @Test
    public void boundaryStraddlingInsertClipped() {
        mFakeAdapter.add(3, 0, 0, 0);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, 5, 8)
                .verify(mOffsetAdapter);
        verify(mDataObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(mDataObserver);
    }

    @Test
    public void boundaryStraddlingRemoveClipped() {
        mFakeAdapter.clear();
        assertThat(mOffsetAdapter.getItemCount()).isEqualTo(0);
        verify(mDataObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mDataObserver);
    }

    private void verifyExpectedOffsetState() {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, 5, 5)
                .verify(mOffsetAdapter);
    }
}
