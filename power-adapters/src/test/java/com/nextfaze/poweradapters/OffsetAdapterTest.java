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
        mFakeAdapter = spy(new FakeAdapter(10));
        mOffsetAdapter = new OffsetAdapter(mFakeAdapter, 5);
        mOffsetAdapter.registerDataObserver(mObserver);
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
        verifyUnchangedState();
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
    public void changeOutOfBoundsDropped() {
        mFakeAdapter.change(2, 1);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void changeOutOfBoundsStateUnchanged() {
        mFakeAdapter.change(4, 1);
        verifyUnchangedState();
    }

    @Test
    public void changeBoundaryStraddlingIsClipped() {
        mFakeAdapter.change(3, 3);
        verify(mObserver).onItemRangeChanged(0, 1);
        verifyRangeState(5, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertOutOfBoundsDropped() {
        mFakeAdapter.insert(1, 1);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void insertOutOfBoundsStateIsCorrect() {
        mFakeAdapter.insert(1, 1);
        verifyRangeState(5, 6);
    }

    @Test
    public void insertBoundaryStraddlingIsClipped() {
        mFakeAdapter.insert(3, 3);
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, 5, 8)
                .verify(mOffsetAdapter);
        verify(mObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeOutOfBoundsDropped() {
        mFakeAdapter.remove(2, 1);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void removeOutOfBoundsStateIsCorrect() {
        mFakeAdapter.remove(2, 1);
        verifyRangeState(5, 4);
    }

    @Test
    public void removeAllStateIsEmpty() {
        mFakeAdapter.clear();
        assertThat(mOffsetAdapter.getItemCount()).isEqualTo(0);
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void moveNotifies() {
        mFakeAdapter.move(1, 3, 2);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    private void verifyRangeState(int start, int count) {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, start, count)
                .verify(mOffsetAdapter);
    }

    private void verifyUnchangedState() {
        AdapterVerifier.verifySubAdapterAllGetCalls()
                .checkRange(mFakeAdapter, 5, 5)
                .verify(mOffsetAdapter);
    }
}
