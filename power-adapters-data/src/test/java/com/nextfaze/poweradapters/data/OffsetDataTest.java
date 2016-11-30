package com.nextfaze.poweradapters.data;

import com.nextfaze.poweradapters.DataObserver;
import lombok.NonNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class OffsetDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private FakeData<String> mFakeData;
    private Data<String> mOffsetData;

    @Before
    public void setUp() throws Exception {
        configure(5, 10);
    }

    private void configure(int offset, int count) {
        mFakeData = spy(new FakeData<String>());
        for (int i = 0; i < count; i++) {
            mFakeData.add(String.valueOf(i));
        }
        mOffsetData = new OffsetData<>(mFakeData, offset);
        mOffsetData.registerDataObserver(mObserver);
        verify(mFakeData).onFirstDataObserverRegistered();
    }

    @Test
    public void negativeOffsetClamped() {
        configure(3, 5);
        OffsetData<String> offsetData = new OffsetData<>(mFakeData, -10);
        assertThat(offsetData.getOffset()).isEqualTo(0);
        assertThat(offsetData).containsExactly("0", "1", "2", "3", "4").inOrder();
    }

    @Test
    public void reducedSize() {
        assertThat(mOffsetData).hasSize(5);
    }

    @Test
    public void clippedContents() {
        assertContains("5", "6", "7", "8", "9");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getOutOfBoundsThrows() {
        mOffsetData.get(6);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getNegativeThrows() {
        mOffsetData.get(-3);
    }

    @Test
    public void changeOutOfBounds() {
        mFakeData.set(4, "a");
        assertContains("5", "6", "7", "8", "9");
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void changeWithinBounds() {
        mFakeData.change(6, "a", "b", "c");
        assertContains("5", "a", "b", "c", "9");
        verify(mObserver).onItemRangeChanged(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void changeBoundaryStraddling() {
        mFakeData.change(3, "a", "b", "c");
        assertContains("c", "6", "7", "8", "9");
        verify(mObserver).onItemRangeChanged(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertOutOfBounds() {
        mFakeData.insert(1, "a");
        assertContains("4", "5", "6", "7", "8", "9");
        verify(mObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertOutOfBounds2() {
        mFakeData.insert(0, "a", "b", "c", "d", "e");
        assertContains("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertBoundaryStraddling() {
        mFakeData.insert(3, "a", "b", "c");
        assertContains("c", "3", "4", "5", "6", "7", "8", "9");
        verify(mObserver).onItemRangeInserted(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmpty() {
        configure(5, 0);
        mFakeData.insert(0, "a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        assertContains("f", "g", "h", "i", "j");
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromBelowOffset() {
        configure(5, 3);
        mFakeData.insert(1, "a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        assertContains("e", "f", "g", "h", "i", "j", "1", "2");
        verify(mObserver).onItemRangeInserted(0, 8);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertWithinBounds() {
        mFakeData.insert(6, "a", "b", "c");
        assertContains("5", "a", "b", "c", "6", "7", "8", "9");
        verify(mObserver).onItemRangeInserted(1, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeOutOfBounds() {
        mFakeData.remove(2, 1);
        assertContains("6", "7", "8", "9");
        verify(mObserver).onItemRangeRemoved(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeWithinBounds() {
        mFakeData.remove(7, 2);
        assertContains("5", "6", "9");
        verify(mObserver).onItemRangeRemoved(2, 2);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddling() {
        mFakeData.remove(3, 5);
        assertEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeBoundaryStraddling2() {
        mFakeData.remove(4, 3);
        assertContains("8", "9");
        verify(mObserver).onItemRangeRemoved(0, 3);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeAll() {
        mFakeData.clear();
        assertEmpty();
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void moveNotifiesOfChange() {
        mFakeData.move(1, 2, 1);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    private void assertContains(@NonNull String... contents) {
        assertThat(mOffsetData).containsExactly((Object[]) contents).inOrder();
    }

    private void assertEmpty() {
        assertContains();
    }

    @NonNull
    private static String[] range(int count) {
        String[] a = new String[count];
        for (int i = 0; i < count; i++) {
            a[i] = String.valueOf(i);
        }
        return a;
    }
}
