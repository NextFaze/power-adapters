package com.nextfaze.poweradapters.data;

import com.nextfaze.poweradapters.DataObserver;
import lombok.NonNull;
import org.junit.Before;
import org.junit.Ignore;
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
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class LimitDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<String> mData;
    private Data<String> mLimitedData;

    @Mock
    private DataObserver mObserver;

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        //noinspection SpellCheckingInspection
        mData.insert(0, "a", "bc", "def", "ghij", "klmno", "pqrstu", "vwxyz12");
        mLimitedData = new LimitData<>(mData, 5);
        mLimitedData.registerDataObserver(mObserver);
    }

    private void configure(int limit, @NonNull String... elements) {
        mData = new FakeData<>();
        mData.insert(0, elements);
        mLimitedData = new LimitData<>(mData, limit);
        mLimitedData.registerDataObserver(mObserver);
    }

    @Test
    public void negativeLimitClamped() {
        assertThat(new LimitData<>(mData, -50)).containsExactly().inOrder();
    }

    @Test
    public void limitedSize() {
        assertThat(mLimitedData).hasSize(5);
    }

    @Test
    public void limitedContents() {
        verifyLimitDataContentsIsClipped();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getOutOfBoundsThrows() {
        mLimitedData.get(7);
    }

    @Test
    public void changeOutOfBounds() {
        mData.set(5, "foo");
        verifyLimitDataContentsIsClipped();
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void changeNormal() {
        mData.change(0, "y", "y", "y");
        verify(mObserver).onItemRangeChanged(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void changeBoundaryStraddlingClipped() {
        mData.change(3, "x", "x", "x");
        verify(mObserver).onItemRangeChanged(3, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertOutOfBounds() {
        mData.add("foo");
        verifyLimitDataContentsIsClipped();
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling() {
        configure(5, "0", "1", "2");
        mData.addAll(1, newArrayList("x", "y", "z"));
        assertThat(mLimitedData).containsExactly("0", "x", "y", "z", "1").inOrder();
        verify(mObserver).onItemRangeRemoved(2, 1);
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling2() {
        configure(5, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        mData.addAll(0, newArrayList("x", "y", "z", "w"));
        assertThat(mLimitedData).containsExactly("x", "y", "z", "w", "0").inOrder();
        verify(mObserver).onItemRangeChanged(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling3() {
        configure(5, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        mData.addAll(4, newArrayList("x", "y"));
        assertThat(mLimitedData).containsExactly("0", "1", "2", "3", "x").inOrder();
        verify(mObserver).onItemRangeChanged(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddlingNonEmptyInitially() {
        configure(3, "a");
        mData.addAll(0, asList("1", "2", "3", "4", "5", "6"));
        assertThat(mLimitedData).containsExactly("1", "2", "3").inOrder();
        verify(mObserver).onItemRangeRemoved(0, 1);
        verify(mObserver).onItemRangeInserted(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddlingNonEmptyInitially2() {
        configure(4, "a", "b");
        mData.addAll(1, asList("1", "2", "3", "4", "5", "6"));
        assertThat(mLimitedData).containsExactly("a", "1", "2", "3").inOrder();
        verify(mObserver).onItemRangeRemoved(1, 1);
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddlingNonEmptyInitially3() {
        configure(7, "a", "b", "c", "d", "e", "f");
        mData.addAll(3, asList("1", "2", "3", "4", "5", "6"));
        assertThat(mLimitedData).containsExactly("a", "b", "c", "1", "2", "3", "4").inOrder();
        verify(mObserver).onItemRangeRemoved(3, 3);
        verify(mObserver).onItemRangeInserted(3, 4);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFullRangeFromStart() {
        configure(5, "0", "1", "2", "3", "4");
        mData.addAll(0, newArrayList("a", "b", "c", "d", "e"));
        assertThat(mLimitedData).containsExactly("a", "b", "c", "d", "e").inOrder();
        verify(mObserver).onItemRangeChanged(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertAppend() {
        configure(5, "a", "b");
        mData.addAll(newArrayList("c", "d"));
        assertThat(mLimitedData).containsExactly("a", "b", "c", "d").inOrder();
        verify(mObserver).onItemRangeInserted(2, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmpty() {
        configure(5);
        mData.addAll(newArrayList("x", "y"));
        assertThat(mLimitedData).containsExactly("x", "y").inOrder();
        verify(mObserver).onItemRangeInserted(0, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeOutOfBoundsDropped() {
        mData.remove("pqrstu");
        verifyLimitDataContentsIsClipped();
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void removeAllClipped() {
        configure(5, "0", "1", "2", "3", "4", "5", "6", "7");
        mData.clear();
        assertThat(mLimitedData).isEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddlingBrokenIntoRemoveAndInsert() {
        configure(5, "0", "1", "2", "3", "4", "6", "7", "8", "9");
        mData.remove(2, 5);
        assertThat(mLimitedData).containsExactly("0", "1", "8", "9").inOrder();
        verify(mObserver).onItemRangeRemoved(2, 3);
        verify(mObserver).onItemRangeInserted(2, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddlingBrokenIntoRemoveAndInsert2() {
        configure(5, "0", "1", "2", "3", "4", "6", "7", "8", "9");
        mData.remove(1, 5);
        assertThat(mLimitedData).containsExactly("0", "7", "8", "9").inOrder();
        verify(mObserver).onItemRangeRemoved(1, 4);
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeFirstTwoFromLargeInnerRangeOnlyIssuesChange() {
        configure(5, "0", "1", "2", "3", "4", "6", "7", "8", "9");
        mData.remove(0, 2);
        assertThat(mLimitedData).containsExactly("2", "3", "4", "6", "7").inOrder();
        verify(mObserver).onItemRangeChanged(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeSingleFromEnd() {
        configure(5, "0", "1", "2", "3", "4");
        mData.remove(4, 1);
        assertThat(mLimitedData).containsExactly("0", "1", "2", "3").inOrder();
        verify(mObserver).onItemRangeRemoved(4, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeSingleFromMiddle() {
        configure(5, "0", "1", "2", "3", "4");
        mData.remove(2, 1);
        assertThat(mLimitedData).containsExactly("0", "1", "3", "4").inOrder();
        verify(mObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Ignore
    @Test
    public void moveOutOfBoundsDropped() {
        // TODO: Check that a move that's entirely out of bounds results in no notification.
        throw new UnsupportedOperationException();
    }

    private void verifyLimitDataContentsIsClipped() {
        assertThat(mLimitedData).containsExactly("a", "bc", "def", "ghij", "klmno").inOrder();
    }
}
