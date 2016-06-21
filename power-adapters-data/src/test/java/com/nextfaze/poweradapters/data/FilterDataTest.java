package com.nextfaze.poweradapters.data;

import com.google.common.collect.FluentIterable;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Predicate;
import lombok.NonNull;
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
import static com.nextfaze.poweradapters.internal.NotificationType.COARSE;
import static java.util.Collections.addAll;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class FilterDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private Data<?> mMockData;

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

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        addAll(mData, "bear", "cat", "foo", "bar", "baz", "fish");
        mFilterData = new FilterData<>(mData, contains("b"));
        mFilterData.registerDataObserver(mFilterDataObserver);
        mFilterData.registerDataObserver(new VerifyingDataObserver(mFilterData));
        mFilterData.registerLoadingObserver(mFilterLoadingObserver);
        mFilterData.registerAvailableObserver(mFilterAvailableObserver);
        mFilterData.registerErrorObserver(mFilterErrorObserver);
    }

    private void assertContentsFiltered() {
        assertThat(mFilterData).containsExactlyElementsIn(FluentIterable.from(mData).filter(new com.google.common.base.Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.contains("b");
            }
        })).inOrder();
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

    @Test(expected = IllegalStateException.class)
    public void getWithoutObservingDataThrows() {
        new FilterData<>(mData, always()).get(0);
    }

    @Test
    public void coarseGrainedChangeDecomposedIntoFineGrained() {
        mData.setNotificationType(COARSE);
        mData.append("bass");
        verify(mFilterDataObserver).onItemRangeChanged(0, 1);
        verify(mFilterDataObserver).onItemRangeChanged(1, 1);
        verify(mFilterDataObserver).onItemRangeChanged(2, 1);
        verify(mFilterDataObserver).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
        verifyZeroInteractions(mFilterLoadingObserver, mFilterAvailableObserver, mFilterErrorObserver);
    }

    @Test
    public void changeIncludedElementNotifies() {
        mData.change(3, "boo");
        verify(mFilterDataObserver).onItemRangeChanged(1, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void changeExcludedToIncludedElementNotifies() {
        mData.change(1, "abba");
        verify(mFilterDataObserver).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void changeIncludedToExcludedElementNotifies() {
        mData.change(4, "fowl");
        verify(mFilterDataObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void appendOfIncludedElementNotifies() {
        mData.append("bro");
        assertContains("bear", "bar", "baz", "bro");
        verify(mFilterDataObserver).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void insertionOfIncludedElementNotifies() {
        mData.insert(2, "baa");
        assertContains("bear", "bar", "baa", "baz");
        verify(mFilterDataObserver).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void insertionOfExcludedElementDoesNotNotify() {
        mData.insert(4, "fowl");
        assertContains("bear","bar", "baz");
        verifyZeroInteractions(mFilterDataObserver);
    }

    @Test
    public void removalOfIncludedElementNotifies() {
        mData.remove("bar");
        verify(mFilterDataObserver).onItemRangeRemoved(1, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void removalOfExcludedElementDoesNotNotify() {
        mData.remove("foo");
        verifyZeroInteractions(mFilterDataObserver);
    }

    @Test
    public void moveForwardsSingle() {
        mData.move(0, 5, 1);
        assertThat(mData).containsExactly("cat", "foo", "bar", "baz", "fish", "bear").inOrder();
        assertContains("bar", "baz", "bear");
        verify(mFilterDataObserver).onItemRangeMoved(0, 2, 1);
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void moveForwardsMultiple() {
        mData.move(0, 2, 2);
        assertThat(mData).containsExactly("foo", "bar", "bear", "cat", "baz", "fish").inOrder();
        assertThat(mFilterData).containsExactly("bar", "bear", "baz").inOrder();
        verify(mFilterDataObserver).onItemRangeMoved(0, 1, 1);
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void moveForwardsExcludedEnd() {
        mData.move(0, 5, 1);
        assertThat(mData).containsExactly("cat", "foo", "bar", "baz", "fish", "bear").inOrder();
        assertContains("bar", "baz", "bear");
        verify(mFilterDataObserver).onItemRangeMoved(0, 2, 1);
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void moveBackwardsMultiple() {
        mData.move(3, 0, 2);
        assertThat(mData).containsExactly("bar", "baz", "bear", "cat", "foo", "fish").inOrder();
        assertContains("bar", "baz", "bear");
        verify(mFilterDataObserver).onItemRangeMoved(1, 0, 2);
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void moveBackwardsExcludedEnd() {
        mData.move(5, 0, 1);
        assertThat(mData).containsExactly("fish", "bear", "cat", "foo", "bar", "baz").inOrder();
        assertContains("bear", "bar", "baz");
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void reassignFilterNotifiesCorrectly() {
        DataObserver observer = mock(DataObserver.class);
        mFilterData.registerDataObserver(observer);
        mFilterData.setPredicate(contains("a"));
        assertContains("bear", "cat", "bar", "baz");
        verify(observer).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void reassignFilterNotifiesCorrectly2() {
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

    private void verifyNoMoreObserverInteractions() {
        verifyNoMoreInteractions(mFilterDataObserver);
        verifyNoMoreInteractions(mFilterLoadingObserver);
        verifyNoMoreInteractions(mFilterAvailableObserver);
        verifyNoMoreInteractions(mFilterErrorObserver);
    }

    @NonNull
    private static Predicate<Object> always() {
        return new Predicate<Object>() {
            @Override
            public boolean apply(Object o) {
                return true;
            }
        };
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

    private void assertContains(@NonNull Object... items) {
        assertThat(mFilterData).containsExactly(items).inOrder();
    }
}
