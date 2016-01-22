package com.nextfaze.powerdata;

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
public final class LimitDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<String> mData;
    private Data<String> mLimitedData;

    @Mock
    private DataObserver mDataObserver;

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        //noinspection SpellCheckingInspection
        addAll(mData, "a", "bc", "def", "ghij", "klmno", "pqrstu", "vwxyz12");
        mLimitedData = new LimitData<>(mData, 5);
        mLimitedData.registerDataObserver(mDataObserver);
    }

    @Test
    public void limitedSize() {
        assertThat(mLimitedData).hasSize(5);
    }

    @Test
    public void limitedContents() {
        assertLimitedDataClippedContents();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getOutOfBoundsThrows() {
        mLimitedData.get(7);
    }

    @Test
    public void outOfBoundsChangeDropped() {
        mData.remove(5);
        assertLimitedDataClippedContents();
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsInsertDropped() {
        mData.add("foo");
        assertLimitedDataClippedContents();
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void outOfBoundsRemoveDropped() {
        mData.remove("pqrstu");
        assertLimitedDataClippedContents();
        verifyZeroInteractions(mDataObserver);
    }

    @Test
    public void boundaryStraddlingChangeClipped() {
        mData.setNotificationsEnabled(false);
        for (int i = 0; i < 3; i++) {
            mData.set(3, "x");
        }
        mData.notifyItemRangeChanged(3, 3);
        verify(mDataObserver).onItemRangeChanged(3, 2);
        verifyNoMoreInteractions(mDataObserver);
    }

    @Test
    public void boundaryStraddlingInsertClipped() {
        mData.addAll(2, newArrayList("x", "y", "z", "w"));
        assertThat(mLimitedData).containsExactly("a", "bc", "x", "y", "z").inOrder();
        verify(mDataObserver).onItemRangeInserted(2, 3);
        verifyNoMoreInteractions(mDataObserver);
    }

    @Test
    public void boundaryStraddlingRemoveClipped() {
        mData.clear();
        assertThat(mLimitedData).isEmpty();
        verify(mDataObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mDataObserver);
    }

    private void assertLimitedDataClippedContents() {
        assertThat(mLimitedData).containsExactly("a", "bc", "def", "ghij", "klmno").inOrder();
    }
}
