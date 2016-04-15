package com.nextfaze.poweradapters.data;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.addAll;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class OffsetDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<String> mData;
    private Data<String> mOffsetData;

    @Mock
    private DataObserver mDataObserver;

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        addAll(mData, "a", "bc", "def", "ghij", "klmno", "pqrstu", "vwxyz12");
        mOffsetData = new OffsetData<>(mData, 5);
        mOffsetData.registerDataObserver(mDataObserver);
    }

    @Test
    public void negativeOffsetClamped() {
        assertThat(new OffsetData<>(mData, -10))
                .containsExactly("a", "bc", "def", "ghij", "klmno", "pqrstu", "vwxyz12").inOrder();
    }

    @Test
    public void reducedSize() {
        assertThat(mOffsetData).hasSize(2);
    }

    @Test
    public void clippedContents() {
        assertOffsetDataClippedContents();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getOutOfBoundsThrows() {
        mOffsetData.get(3);
    }

    @Test
    public void outOfBoundsChangeDropped() {
        mData.set(4, "foo");
        assertOffsetDataClippedContents();
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsInsertDropped() {
        mData.add(1, "foo");
        assertThat(mOffsetData).containsExactly("klmno", "pqrstu", "vwxyz12").inOrder();
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsRemoveDropped() {
        mData.remove("def");
        assertThat(mOffsetData).containsExactly("vwxyz12").inOrder();
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsMoveDropped() {
        // TODO: Check that a move that's entirely out of bounds results in no notification.
        throw new UnsupportedOperationException();
    }

    @Test
    public void boundaryStraddlingChangeClipped() {
        mData.setNotificationsEnabled(false);
        for (int i = 0; i < 3; i++) {
            mData.set(3, "x");
        }
        mData.notifyItemRangeChanged(3, 3);
        verify(mDataObserver).onItemRangeChanged(0, 1);
        verifyNoMoreInteractions(mDataObserver);
    }

    @Test
    public void boundaryStraddlingInsertClipped() {
        mData.addAll(2, newArrayList("x", "y", "z", "w"));
        assertThat(mOffsetData).containsExactly("w", "def", "ghij", "klmno", "pqrstu", "vwxyz12").inOrder();
        verify(mDataObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(mDataObserver);
    }

    @Test
    public void boundaryStraddlingRemoveClipped() {
        mData.clear();
        assertThat(mOffsetData).isEmpty();
        verify(mDataObserver).onItemRangeRemoved(0, 2);
        verifyNoMoreInteractions(mDataObserver);
    }

    private void assertOffsetDataClippedContents() {
        assertThat(mOffsetData).containsExactly("pqrstu", "vwxyz12").inOrder();
    }
}
