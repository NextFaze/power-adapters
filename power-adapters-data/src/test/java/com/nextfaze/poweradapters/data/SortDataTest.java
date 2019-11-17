package com.nextfaze.poweradapters.data;

import com.google.common.collect.Ordering;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.data.test.FakeData;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import androidx.annotation.NonNull;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.sort;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public final class SortDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<Integer> mFakeData;

    private SortData<Integer> mSortData;
    private VerifyingDataObserver mVerifyingObserver;

    @Before
    public void setUp() throws Exception {
        mFakeData = new FakeData<>();
        mFakeData.insert(0, 0, 10, 5, 15, 25, 20);
        mSortData = new SortData<>(mFakeData, Ordering.natural());
        mVerifyingObserver = new VerifyingDataObserver(mSortData);
        mSortData.registerDataObserver(mVerifyingObserver);
    }

    @After
    public void tearDown() throws Exception {
        mVerifyingObserver.assertSizeConsistent();
    }

    @Test
    public void elementsSorted() {
        assertContentsSorted();
    }

    @Test
    public void sizeIndicatesFullyAccessibleRangeWhenNoObserversRegistered() {
        FakeData<Integer> fakeData = new FakeData<>();
        fakeData.insert(0, 1, 2, 3);
        SortData<Integer> sortData = new SortData<>(fakeData, Ordering.<Integer>natural());
        int size = sortData.size();
        for (int i = 0; i < size; i++) {
            sortData.get(i);
        }
    }

    @Test
    public void changeSingle1() {
        DataObserver observer = registerMockObserver();
        mFakeData.change(1, 16);
        assertContentsSorted();
        verify(observer).onItemRangeRemoved(2, 1);
        verify(observer).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void changeSingle2() {
        DataObserver observer = registerMockObserver();
        mFakeData.change(3, 4);
        assertContentsSorted();
        verify(observer).onItemRangeRemoved(3, 1);
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void changeSingle3() {
        DataObserver observer = registerMockObserver();
        mFakeData.change(4, 26);
        assertContentsSorted();
        verify(observer).onItemRangeRemoved(5, 1);
        verify(observer).onItemRangeInserted(5, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void changeMultiple1() {
        DataObserver observer = registerMockObserver();
        mFakeData.change(1, 18, 29, 1);
        assertContentsSorted();
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onItemRangeRemoved(2, 1);
        inOrder.verify(observer).onItemRangeInserted(3, 1);
        inOrder.verify(observer).onItemRangeRemoved(1, 1);
        inOrder.verify(observer).onItemRangeInserted(5, 1);
        inOrder.verify(observer).onItemRangeRemoved(1, 1);
        inOrder.verify(observer).onItemRangeInserted(1, 1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void insertSingle() {
        DataObserver observer = registerMockObserver();
        mFakeData.insert(3, 1);
        assertContentsSorted();
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void insertMultiple1() {
        DataObserver observer = registerMockObserver();
        mFakeData.insert(2, 1, 6, 17);
        assertContentsSorted();
        verify(observer).onItemRangeInserted(1, 1);
        verify(observer).onItemRangeInserted(3, 1);
        verify(observer).onItemRangeInserted(6, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void insertMultiple2() {
        DataObserver observer = registerMockObserver();
        mFakeData.insert(0, 26, 3);
        assertContentsSorted();
        verify(observer).onItemRangeInserted(6, 1);
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void removeSingle() {
        DataObserver observer = registerMockObserver();
        mFakeData.remove(1, 1);
        assertContentsSorted();
        verify(observer).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void removeMultiple1() {
        DataObserver observer = registerMockObserver();
        mFakeData.remove(0, 4);
        assertContentsSorted();
        verify(observer, times(3)).onItemRangeRemoved(0, 1);
        verify(observer, times(1)).onItemRangeRemoved(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void removeMultiple2() {
        DataObserver observer = registerMockObserver();
        mFakeData.remove(1, 3);
        assertContentsSorted();
        verify(observer).onItemRangeRemoved(2, 1);
        verify(observer, times(2)).onItemRangeRemoved(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @NonNull
    private DataObserver registerMockObserver() {
        DataObserver observer = mock(DataObserver.class);
        mSortData.registerDataObserver(observer);
        return observer;
    }

    private void assertContentsSorted() {
        List<Integer> sortedFakeItems = newArrayList(mFakeData);
        sort(sortedFakeItems);
        System.out.println("Contents: " + newArrayList(mSortData));
        assertThat(mSortData).containsExactlyElementsIn(sortedFakeItems).inOrder();
    }
}
