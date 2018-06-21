package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;

import com.google.common.collect.FluentIterable;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Predicate;
import com.nextfaze.poweradapters.data.test.FakeData;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static com.nextfaze.poweradapters.internal.NotificationType.COARSE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public final class FilterDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<String> mData;
    private FilterData<String> mFilterData;

    @Mock
    private DataObserver mFilterDataObserver;

    @Mock
    private LoadingObserver mFilterLoadingObserver;

    @Mock
    private AvailableObserver mFilterAvailableObserver;

    @Mock
    private ErrorObserver mFilterErrorObserver;
    private VerifyingDataObserver mVerifyingObserver;

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        mData.insert(0, "bear", "cat", "foo", "bar", "baz", "fish");
        mFilterData = new FilterData<>(mData, contains("b"));
        mFilterData.registerDataObserver(mFilterDataObserver);
        mVerifyingObserver = new VerifyingDataObserver(mFilterData);
        mFilterData.registerDataObserver(mVerifyingObserver);
        mFilterData.registerLoadingObserver(mFilterLoadingObserver);
        mFilterData.registerAvailableObserver(mFilterAvailableObserver);
        mFilterData.registerErrorObserver(mFilterErrorObserver);
    }

    @After
    public void tearDown() throws Exception {
        mVerifyingObserver.assertSizeConsistent();
    }

    @Test
    public void includedElementsPresent() {
        assertContentsFiltered();
    }

    @Test
    public void excludedElementsAbsent() {
        assertThat(mFilterData).containsNoneOf("foo", "fish");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void invalidGetIndexThrowsIndexOutOfBounds() {
        mFilterData.get(5);
    }

    @Test
    public void sizeIndicatesFullyAccessibleRangeWhenNoObserversRegistered() {
        FakeData<String> fakeData = new FakeData<>();
        fakeData.insert(0, "a", "b", "c");
        FilterData<String> filterData = new FilterData<>(fakeData);
        int size = filterData.size();
        for (int i = 0; i < size; i++) {
            filterData.get(i);
        }
    }

    @Test
    public void coarseGrainedChangeDecomposedIntoFineGrained() {
        DataObserver observer = registerMockObserver();
        mData.setNotificationType(COARSE);
        mData.append("bass");
        verify(observer).onItemRangeChanged(0, 1);
        verify(observer).onItemRangeChanged(1, 1);
        verify(observer).onItemRangeChanged(2, 1);
        verify(observer).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(observer);
        verifyZeroInteractions(mFilterLoadingObserver, mFilterAvailableObserver, mFilterErrorObserver);
    }

    @Test
    public void changeIncludedElement() {
        DataObserver observer = registerMockObserver();
        mData.change(3, "boo");
        verify(observer).onItemRangeChanged(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void changeExcludedToIncludedElement() {
        DataObserver observer = registerMockObserver();
        mData.change(1, "abba");
        assertContains("bear", "abba", "bar", "baz");
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void changeIncludedToExcludedElement() {
        DataObserver observer = registerMockObserver();
        mData.change(4, "fowl");
        assertContains("bear", "bar");
        verify(observer).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void appendOfIncludedElement() {
        DataObserver observer = registerMockObserver();
        mData.append("bro");
        assertContains("bear", "bar", "baz", "bro");
        verify(observer).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void insertionOfIncludedElement() {
        DataObserver observer = registerMockObserver();
        mData.insert(2, "baa");
        assertContains("bear", "baa", "bar", "baz");
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void insertionOfExcludedElement() {
        DataObserver observer = registerMockObserver();
        mData.insert(4, "fowl");
        assertContains("bear", "bar", "baz");
        verifyZeroInteractions(observer);
    }

    @Test
    public void removalOfIncludedElement() {
        DataObserver observer = registerMockObserver();
        mData.remove("bar");
        assertContains("bear", "baz");
        verify(observer).onItemRangeRemoved(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void removalOfExcludedElement() {
        DataObserver observer = registerMockObserver();
        mData.remove("foo");
        assertContains("bear", "bar", "baz");
        verifyZeroInteractions(observer);
    }

    @Test
    public void removeAll() {
        DataObserver observer = registerMockObserver();
        mData.clear();
        assertContains();
        verify(observer).onItemRangeRemoved(0, 1);
        verify(observer).onItemRangeRemoved(1, 1);
        verify(observer).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void moveForwardsSingle() {
        DataObserver observer = registerMockObserver();
        mData.move(0, 5, 1);
        assertThat(mData).containsExactly("cat", "foo", "bar", "baz", "fish", "bear").inOrder();
        assertContains("bar", "baz", "bear");
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void moveForwardsMultiple() {
        DataObserver observer = registerMockObserver();
        mData.move(0, 2, 2);
        assertThat(mData).containsExactly("foo", "bar", "bear", "cat", "baz", "fish").inOrder();
        assertThat(mFilterData).containsExactly("bar", "bear", "baz").inOrder();
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void moveForwardsExcludedEnd() {
        DataObserver observer = registerMockObserver();
        mData.move(0, 5, 1);
        assertThat(mData).containsExactly("cat", "foo", "bar", "baz", "fish", "bear").inOrder();
        assertContains("bar", "baz", "bear");
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void moveBackwardsMultiple() {
        DataObserver observer = registerMockObserver();
        mData.move(3, 0, 2);
        assertThat(mData).containsExactly("bar", "baz", "bear", "cat", "foo", "fish").inOrder();
        assertContains("bar", "baz", "bear");
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void moveBackwardsExcludedEnd() {
        DataObserver observer = registerMockObserver();
        mData.move(5, 0, 1);
        assertThat(mData).containsExactly("fish", "bear", "cat", "foo", "bar", "baz").inOrder();
        assertContains("bear", "bar", "baz");
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void reassignFilter() {
        DataObserver observer = mock(DataObserver.class);
        mFilterData.registerDataObserver(observer);
        mFilterData.setPredicate(contains("a"));
        assertContains("bear", "cat", "bar", "baz");
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void reassignFilter2() {
        DataObserver observer = mock(DataObserver.class);
        mFilterData.registerDataObserver(observer);
        mFilterData.setPredicate(contains("f"));
        assertContains("foo", "fish");
        verify(observer).onItemRangeRemoved(0, 1);
        verify(observer).onItemRangeInserted(0, 1);
        verify(observer, times(2)).onItemRangeRemoved(1, 1);
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @NonNull
    private static Predicate<String> contains(@NonNull final String substring) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.contains(substring);
            }
        };
    }

    @NonNull
    private DataObserver registerMockObserver() {
        DataObserver observer = mock(DataObserver.class);
        mFilterData.registerDataObserver(observer);
        return observer;
    }

    private void assertContains(@NonNull Object... items) {
        System.out.println("Contents: " + newArrayList(mFilterData));
        assertThat(mFilterData).containsExactly(items).inOrder();
    }

    private void assertContentsFiltered() {
        assertThat(mFilterData).containsExactlyElementsIn(FluentIterable.from(mData).filter(new com.google.common.base.Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.contains("b");
            }
        })).inOrder();
    }
}
