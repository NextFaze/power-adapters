package com.nextfaze.asyncdata;

import com.android.internal.util.Predicate;
import lombok.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.addAll;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FilterDataTest {

    @Mock
    private Data<?> mMockData;

    private FilterData<?> mFilterMockData;
    private FilterData<?> mFilterFakeData;

    @Mock
    private LoadingObserver mFilterFakeLoadingObserver;

    @Mock
    private AvailableObserver mFilterFakeAvailableObserver;

    private FakeData<String> mData;
    private Data<String> mFilterData;

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
        MockitoAnnotations.initMocks(this);

        mData = new FakeData<>();

        // Mocks
        mFilterMockData = new FilterData<>(mMockData, always());

        // Fakes
        mFilterFakeData = new FilterData<>(mData, always());
        mFilterFakeData.registerLoadingObserver(mFilterFakeLoadingObserver);
        mFilterFakeData.registerAvailableObserver(mFilterFakeAvailableObserver);

        // Real
        addAll(mData, "bear", "cat", "foo", "bar", "baz", "fish");
        mFilterData = new FilterData<>(mData, contains("b"));
        mFilterData.registerDataObserver(mFilterDataObserver);
        mFilterData.registerLoadingObserver(mFilterLoadingObserver);
        mFilterData.registerAvailableObserver(mFilterAvailableObserver);
        mFilterData.registerErrorObserver(mFilterErrorObserver);
    }

    @Test
    public void invalidateForwarded() {
        mFilterMockData.invalidate();
        verify(mMockData).invalidate();
        verifyNoMoreInteractions(mMockData);
    }

    @Test
    public void refreshForwarded() {
        mFilterMockData.refresh();
        verify(mMockData).refresh();
        verifyNoMoreInteractions(mMockData);
    }

    @Test
    public void reloadForwarded() {
        mFilterMockData.reload();
        verify(mMockData).reload();
        verifyNoMoreInteractions(mMockData);
    }

    @Test
    public void loadingStatePropagated() {
        mData.setLoading(true);
        assertTrue(mFilterFakeData.isLoading());
        mData.setLoading(false);
        assertFalse(mFilterFakeData.isLoading());
        verify(mFilterFakeLoadingObserver, times(2)).onLoadingChange();
        verifyNoMoreInteractions(mFilterFakeLoadingObserver);
    }

    @Test
    public void availableStatePropagated() {
        mData.setAvailable(25);
        verify(mFilterFakeAvailableObserver).onAvailableChange();
        verifyNoMoreInteractions(mFilterFakeAvailableObserver);
        assertEquals(25, mFilterFakeData.available());
    }

    @Test
    public void errorsPropagated() {
        RuntimeException exception = new RuntimeException();
        mData.notifyError(exception);
        verify(mFilterErrorObserver).onError(exception);
        verifyNoMoreInteractions(mFilterErrorObserver);
        verifyZeroInteractions(mFilterDataObserver);
        verifyZeroInteractions(mFilterLoadingObserver);
        verifyZeroInteractions(mFilterAvailableObserver);
    }

    @Test
    public void doesNotActOnNotificationsAfterClose() {
        mFilterData.close();
        mData.add("bas");
        mData.setAvailable(9);
        mData.setLoading(true);
        mData.notifyError(new RuntimeException());
        verifyZeroInteractions(mFilterDataObserver);
        verifyZeroInteractions(mFilterLoadingObserver);
        verifyZeroInteractions(mFilterAvailableObserver);
        verifyZeroInteractions(mFilterErrorObserver);
    }

    @Test
    public void includedElementsPresent() {
        assertThat(mFilterData).containsExactly("bear", "bar", "baz").inOrder();
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
    public void changeIncludedElementNotifies() {
        mData.set(3, "boo");
        verify(mFilterDataObserver).onItemRangeChanged(1, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void changeExcludedToIncludedElementNotifies() {
        mData.set(1, "abba");
        verify(mFilterDataObserver).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void changeIncludedToExcludedElementNotifies() {
        mData.set(4, "fowl");
        verify(mFilterDataObserver).onItemRangeRemoved(2, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void appendOfIncludedElementNotifies() {
        mData.add("bro");
        verify(mFilterDataObserver).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void insertionOfIncludedElementNotifies() {
        mData.add(2, "baa");
        verify(mFilterDataObserver).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
    }

    @Test
    public void insertionOfExcludedElementDoesNotNotify() {
        mData.add(4, "fowl");
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
        // "bear", "cat", "foo", "bar", "baz", "fish" / "bear", "bar", "baz" to
        // "cat", "foo", "bar", "baz", "fish", "bear" / "bar", "baz", "bear"
        mData.move(0, 5, 1);
        assertThat(mData).containsExactly("cat", "foo", "bar", "baz", "fish", "bear").inOrder();
        assertThat(mFilterData).containsExactly("bar", "baz", "bear").inOrder();
        verify(mFilterDataObserver).onItemRangeMoved(0, 2, 1);
    }

    @Test
    public void moveForwardsMultiple() {
        //  "bear", "cat", "foo", "bar", "baz", "fish" / "bear", "bar", "baz" to
        // "foo", "bar", "bear", "cat", "baz", "fish" / "bar", "bear", "baz"
        mData.move(0, 2, 2);
        assertThat(mData).containsExactly("foo", "bar", "bear", "cat", "baz", "fish").inOrder();
        assertThat(mFilterData).containsExactly("bar", "bear", "baz").inOrder();
        verify(mFilterDataObserver).onItemRangeMoved(0, 1, 1);
    }

    @Test
    public void moveForwardsExcludedEnd() {
        mData.move(0, 5, 1);
        assertThat(mData).containsExactly("cat", "foo", "bar", "baz", "fish", "bear").inOrder();
        assertThat(mFilterData).containsExactly("bar", "baz", "bear").inOrder();
        verify(mFilterDataObserver).onItemRangeMoved(0, 2, 1);
    }

    @Test
    public void moveBackwardsMultiple() {
        // "bear", "cat", "foo", "bar", "baz", "fish" / "bear", "bar", "baz" to
        // "bar", "baz", "bear", "cat", "foo", "fish" / "bar", "baz", "bear"
        mData.move(3, 0, 2);
        assertThat(mData).containsExactly("bar", "baz", "bear", "cat", "foo", "fish").inOrder();
        assertThat(mFilterData).containsExactly("bar", "baz", "bear").inOrder();
        verify(mFilterDataObserver).onItemRangeMoved(1, 0, 2);
    }

    @Test
    public void moveBackwardsExcludedEnd() {
        mData.move(5, 0, 1);
        assertThat(mData).containsExactly("fish", "bear", "cat", "foo", "bar", "baz").inOrder();
        assertThat(mFilterData).containsExactly("bear", "bar", "baz").inOrder();
        verifyZeroInteractions(mFilterDataObserver);
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
}
