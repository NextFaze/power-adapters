package com.nextfaze.asyncdata;

import com.android.internal.util.Predicate;
import lombok.NonNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.addAll;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FilterDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

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
        verifyZeroInteractions(mFilterDataObserver, mFilterLoadingObserver, mFilterAvailableObserver);
    }

    @Test
    public void doesNotActOnNotificationsAfterClose() {
        mFilterData.close();
        mData.add("bas");
        mData.setAvailable(9);
        mData.setLoading(true);
        mData.notifyError(new RuntimeException());
        verifyZeroObserverInteractions();
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
    public void coarseGrainedChangeDecomposedIntoFineGrained() {
        mData.setNotificationsEnabled(false);
        mData.add("bass");
        mData.notifyDataChanged();
        verify(mFilterDataObserver).onItemRangeChanged(0, 1);
        verify(mFilterDataObserver).onItemRangeChanged(1, 1);
        verify(mFilterDataObserver).onItemRangeChanged(2, 1);
        verify(mFilterDataObserver).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(mFilterDataObserver);
        verifyZeroInteractions(mFilterLoadingObserver, mFilterAvailableObserver, mFilterErrorObserver);
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
        mData.move(0, 5, 1);
        assertThat(mData).containsExactly("cat", "foo", "bar", "baz", "fish", "bear").inOrder();
        assertThat(mFilterData).containsExactly("bar", "baz", "bear").inOrder();
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
        assertThat(mFilterData).containsExactly("bar", "baz", "bear").inOrder();
        verify(mFilterDataObserver).onItemRangeMoved(0, 2, 1);
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void moveBackwardsMultiple() {
        mData.move(3, 0, 2);
        assertThat(mData).containsExactly("bar", "baz", "bear", "cat", "foo", "fish").inOrder();
        assertThat(mFilterData).containsExactly("bar", "baz", "bear").inOrder();
        verify(mFilterDataObserver).onItemRangeMoved(1, 0, 2);
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void moveBackwardsExcludedEnd() {
        mData.move(5, 0, 1);
        assertThat(mData).containsExactly("fish", "bear", "cat", "foo", "bar", "baz").inOrder();
        assertThat(mFilterData).containsExactly("bear", "bar", "baz").inOrder();
        verifyNoMoreObserverInteractions();
    }

    @Test
    public void dataObserverRegistration() {
        DataObserver dataObserver = mock(DataObserver.class);
        mFilterData.registerDataObserver(dataObserver);
        mData.add("boo");
        verify(dataObserver).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void dataObserverUnregistration() {
        DataObserver dataObserver = mock(DataObserver.class);
        mFilterData.registerDataObserver(dataObserver);
        mFilterData.unregisterDataObserver(dataObserver);
        mData.add("bass");
        verifyZeroInteractions(dataObserver);
    }

    @Test
    public void registersWithWrappedDataWhileExternalDataObserversPresent() {
        Data<?> data = mock(Data.class);
        FilterData<?> filter = new FilterData<>(data, always());
        DataObserver dataObserver1 = mock(DataObserver.class);
        DataObserver dataObserver2 = mock(DataObserver.class);
        filter.registerDataObserver(dataObserver1);
        filter.registerDataObserver(dataObserver2);
        filter.unregisterDataObserver(dataObserver1);
        filter.unregisterDataObserver(dataObserver2);
        ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
        verify(data).registerDataObserver(captor.capture());
        verify(data).unregisterDataObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersWithWrappedDataWhileExternalLoadingObserversPresent() {
        Data<?> data = mock(Data.class);
        FilterData<?> filter = new FilterData<>(data, always());
        LoadingObserver loadingObserver1 = mock(LoadingObserver.class);
        LoadingObserver loadingObserver2 = mock(LoadingObserver.class);
        filter.registerLoadingObserver(loadingObserver1);
        filter.registerLoadingObserver(loadingObserver2);
        filter.unregisterLoadingObserver(loadingObserver1);
        filter.unregisterLoadingObserver(loadingObserver2);
        ArgumentCaptor<LoadingObserver> captor = ArgumentCaptor.forClass(LoadingObserver.class);
        verify(data).registerLoadingObserver(captor.capture());
        verify(data).unregisterLoadingObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersWithWrappedDataWhileExternalAvailableObserversPresent() {
        Data<?> data = mock(Data.class);
        FilterData<?> filter = new FilterData<>(data, always());
        AvailableObserver availableObserver1 = mock(AvailableObserver.class);
        AvailableObserver availableObserver2 = mock(AvailableObserver.class);
        filter.registerAvailableObserver(availableObserver1);
        filter.registerAvailableObserver(availableObserver2);
        filter.unregisterAvailableObserver(availableObserver1);
        filter.unregisterAvailableObserver(availableObserver2);
        ArgumentCaptor<AvailableObserver> captor = ArgumentCaptor.forClass(AvailableObserver.class);
        verify(data).registerAvailableObserver(captor.capture());
        verify(data).unregisterAvailableObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersWithWrappedDataWhileExternalErrorObserversPresent() {
        Data<?> data = mock(Data.class);
        FilterData<?> filter = new FilterData<>(data, always());
        ErrorObserver errorObserver1 = mock(ErrorObserver.class);
        ErrorObserver errorObserver2 = mock(ErrorObserver.class);
        filter.registerErrorObserver(errorObserver1);
        filter.registerErrorObserver(errorObserver2);
        filter.unregisterErrorObserver(errorObserver1);
        filter.unregisterErrorObserver(errorObserver2);
        ArgumentCaptor<ErrorObserver> captor = ArgumentCaptor.forClass(ErrorObserver.class);
        verify(data).registerErrorObserver(captor.capture());
        verify(data).unregisterErrorObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void loadingObserverRegistration() {
        LoadingObserver loadingObserver = mock(LoadingObserver.class);
        mFilterData.registerLoadingObserver(loadingObserver);
        mData.setLoading(true);
        verify(loadingObserver).onLoadingChange();
        verifyNoMoreInteractions(loadingObserver);
    }

    @Test
    public void loadingObserverUnregistration() {
        LoadingObserver loadingObserver = mock(LoadingObserver.class);
        mFilterData.registerLoadingObserver(loadingObserver);
        mFilterData.unregisterLoadingObserver(loadingObserver);
        mData.setLoading(true);
        verifyZeroInteractions(loadingObserver);
    }

    @Test
    public void availableObserverRegistration() {
        AvailableObserver availableObserver = mock(AvailableObserver.class);
        mFilterData.registerAvailableObserver(availableObserver);
        mData.setAvailable(92);
        verify(availableObserver).onAvailableChange();
        verifyNoMoreInteractions(availableObserver);
    }

    @Test
    public void availableObserverUnregistration() {
        AvailableObserver availableObserver = mock(AvailableObserver.class);
        mFilterData.registerAvailableObserver(availableObserver);
        mFilterData.unregisterAvailableObserver(availableObserver);
        mData.setAvailable(92);
        verifyZeroInteractions(availableObserver);
    }

    @Test
    public void errorObserverRegistration() {
        ErrorObserver errorObserver = mock(ErrorObserver.class);
        mFilterData.registerErrorObserver(errorObserver);
        RuntimeException exception = new RuntimeException();
        mData.notifyError(exception);
        verify(errorObserver).onError(exception);
        verifyNoMoreInteractions(errorObserver);
    }

    @Test
    public void errorObserverUnregistration() {
        ErrorObserver errorObserver = mock(ErrorObserver.class);
        mFilterData.registerErrorObserver(errorObserver);
        mFilterData.unregisterErrorObserver(errorObserver);
        mData.notifyError(new RuntimeException());
        verifyZeroInteractions(errorObserver);
    }

    private void verifyNoMoreObserverInteractions() {
        verifyNoMoreInteractions(mFilterDataObserver);
        verifyNoMoreInteractions(mFilterLoadingObserver);
        verifyNoMoreInteractions(mFilterAvailableObserver);
        verifyNoMoreInteractions(mFilterErrorObserver);
    }

    private void verifyZeroObserverInteractions() {
        verifyZeroInteractions(mFilterDataObserver);
        verifyZeroInteractions(mFilterLoadingObserver);
        verifyZeroInteractions(mFilterAvailableObserver);
        verifyZeroInteractions(mFilterErrorObserver);
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
