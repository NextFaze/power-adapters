package com.nextfaze.asyncdata;

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
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class DataWrapperTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<String> mData;

    private Data<?> mDataWrapper;

    @Mock
    private Data<?> mMockData;

    private Data<?> mDataWrapperAroundMockData;

    @Mock
    private DataObserver mWrapperDataObserver;

    @Mock
    private LoadingObserver mWrapperLoadingObserver;

    @Mock
    private AvailableObserver mWrapperAvailableObserver;

    @Mock
    private ErrorObserver mWrapperErrorObserver;

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        mDataWrapperAroundMockData = newDataWrapper(mMockData);
        mDataWrapper = newDataWrapper(mData);
        mDataWrapper.registerDataObserver(mWrapperDataObserver);
        mDataWrapper.registerLoadingObserver(mWrapperLoadingObserver);
        mDataWrapper.registerAvailableObserver(mWrapperAvailableObserver);
        mDataWrapper.registerErrorObserver(mWrapperErrorObserver);
    }

    @Test
    public void invalidateForwarded() {
        mDataWrapperAroundMockData.invalidate();
        verify(mMockData).invalidate();
        verifyNoMoreInteractions(mMockData);
    }

    @Test
    public void refreshForwarded() {
        mDataWrapperAroundMockData.refresh();
        verify(mMockData).refresh();
        verifyNoMoreInteractions(mMockData);
    }

    @Test
    public void reloadForwarded() {
        mDataWrapperAroundMockData.reload();
        verify(mMockData).reload();
        verifyNoMoreInteractions(mMockData);
    }

    @Test
    public void loadingStatePropagated() {
        mData.setLoading(true);
        assertTrue(mDataWrapper.isLoading());
        mData.setLoading(false);
        assertFalse(mDataWrapper.isLoading());
        verify(mWrapperLoadingObserver, times(2)).onLoadingChange();
        verifyNoMoreInteractions(mWrapperLoadingObserver);
    }

    @Test
    public void availableStatePropagated() {
        mData.setAvailable(25);
        verify(mWrapperAvailableObserver).onAvailableChange();
        verifyNoMoreInteractions(mWrapperAvailableObserver);
        assertEquals(25, mDataWrapper.available());
    }

    @Test
    public void errorsPropagated() {
        RuntimeException exception = new RuntimeException();
        mData.notifyError(exception);
        verify(mWrapperErrorObserver).onError(exception);
        verifyNoMoreInteractions(mWrapperErrorObserver);
        verifyZeroInteractions(mWrapperDataObserver, mWrapperLoadingObserver, mWrapperAvailableObserver);
    }

    @Test
    public void doesNotActOnNotificationsAfterClose() {
        mDataWrapper.close();
        mData.add("a");
        mData.setAvailable(9);
        mData.setLoading(true);
        mData.notifyError(new RuntimeException());
        verifyZeroObserverInteractions();
    }

    @Test
    public void dataObserverRegistration() {
        DataObserver dataObserver = mock(DataObserver.class);
        mDataWrapper.registerDataObserver(dataObserver);
        mData.add("a");
        verify(dataObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void dataObserverUnregistration() {
        DataObserver dataObserver = mock(DataObserver.class);
        mDataWrapper.registerDataObserver(dataObserver);
        mDataWrapper.unregisterDataObserver(dataObserver);
        mData.add("a");
        verifyZeroInteractions(dataObserver);
    }

    @Test
    public void loadingObserverRegistration() {
        LoadingObserver loadingObserver = mock(LoadingObserver.class);
        mDataWrapper.registerLoadingObserver(loadingObserver);
        mData.setLoading(true);
        verify(loadingObserver).onLoadingChange();
        verifyNoMoreInteractions(loadingObserver);
    }

    @Test
    public void loadingObserverUnregistration() {
        LoadingObserver loadingObserver = mock(LoadingObserver.class);
        mDataWrapper.registerLoadingObserver(loadingObserver);
        mDataWrapper.unregisterLoadingObserver(loadingObserver);
        mData.setLoading(true);
        verifyZeroInteractions(loadingObserver);
    }

    @Test
    public void availableObserverRegistration() {
        AvailableObserver availableObserver = mock(AvailableObserver.class);
        mDataWrapper.registerAvailableObserver(availableObserver);
        mData.setAvailable(92);
        verify(availableObserver).onAvailableChange();
        verifyNoMoreInteractions(availableObserver);
    }

    @Test
    public void availableObserverUnregistration() {
        AvailableObserver availableObserver = mock(AvailableObserver.class);
        mDataWrapper.registerAvailableObserver(availableObserver);
        mDataWrapper.unregisterAvailableObserver(availableObserver);
        mData.setAvailable(92);
        verifyZeroInteractions(availableObserver);
    }

    @Test
    public void errorObserverRegistration() {
        ErrorObserver errorObserver = mock(ErrorObserver.class);
        mDataWrapper.registerErrorObserver(errorObserver);
        RuntimeException exception = new RuntimeException();
        mData.notifyError(exception);
        verify(errorObserver).onError(exception);
        verifyNoMoreInteractions(errorObserver);
    }

    @Test
    public void errorObserverUnregistration() {
        ErrorObserver errorObserver = mock(ErrorObserver.class);
        mDataWrapper.registerErrorObserver(errorObserver);
        mDataWrapper.unregisterErrorObserver(errorObserver);
        mData.notifyError(new RuntimeException());
        verifyZeroInteractions(errorObserver);
    }

    @Test
    public void registersWithWrappedDataWhileExternalDataObserversPresent() {
        Data<?> data = mock(Data.class);
        Data<?> wrapper = newDataWrapper(data);
        DataObserver dataObserver1 = mock(DataObserver.class);
        DataObserver dataObserver2 = mock(DataObserver.class);
        wrapper.registerDataObserver(dataObserver1);
        wrapper.registerDataObserver(dataObserver2);
        wrapper.unregisterDataObserver(dataObserver1);
        wrapper.unregisterDataObserver(dataObserver2);
        ArgumentCaptor<DataObserver> captor = ArgumentCaptor.forClass(DataObserver.class);
        verify(data).registerDataObserver(captor.capture());
        verify(data).unregisterDataObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersWithWrappedDataWhileExternalLoadingObserversPresent() {
        Data<?> data = mock(Data.class);
        Data<?> wrapper = newDataWrapper(data);
        LoadingObserver loadingObserver1 = mock(LoadingObserver.class);
        LoadingObserver loadingObserver2 = mock(LoadingObserver.class);
        wrapper.registerLoadingObserver(loadingObserver1);
        wrapper.registerLoadingObserver(loadingObserver2);
        wrapper.unregisterLoadingObserver(loadingObserver1);
        wrapper.unregisterLoadingObserver(loadingObserver2);
        ArgumentCaptor<LoadingObserver> captor = ArgumentCaptor.forClass(LoadingObserver.class);
        verify(data).registerLoadingObserver(captor.capture());
        verify(data).unregisterLoadingObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersWithWrappedDataWhileExternalAvailableObserversPresent() {
        Data<?> data = mock(Data.class);
        Data<?> wrapper = newDataWrapper(data);
        AvailableObserver availableObserver1 = mock(AvailableObserver.class);
        AvailableObserver availableObserver2 = mock(AvailableObserver.class);
        wrapper.registerAvailableObserver(availableObserver1);
        wrapper.registerAvailableObserver(availableObserver2);
        wrapper.unregisterAvailableObserver(availableObserver1);
        wrapper.unregisterAvailableObserver(availableObserver2);
        ArgumentCaptor<AvailableObserver> captor = ArgumentCaptor.forClass(AvailableObserver.class);
        verify(data).registerAvailableObserver(captor.capture());
        verify(data).unregisterAvailableObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    public void registersWithWrappedDataWhileExternalErrorObserversPresent() {
        Data<?> data = mock(Data.class);
        Data<?> wrapper = newDataWrapper(data);
        ErrorObserver errorObserver1 = mock(ErrorObserver.class);
        ErrorObserver errorObserver2 = mock(ErrorObserver.class);
        wrapper.registerErrorObserver(errorObserver1);
        wrapper.registerErrorObserver(errorObserver2);
        wrapper.unregisterErrorObserver(errorObserver1);
        wrapper.unregisterErrorObserver(errorObserver2);
        ArgumentCaptor<ErrorObserver> captor = ArgumentCaptor.forClass(ErrorObserver.class);
        verify(data).registerErrorObserver(captor.capture());
        verify(data).unregisterErrorObserver(eq(captor.getValue()));
        assertThat(captor.getValue()).isNotNull();
    }

    @NonNull
    private static DataWrapper<?> newDataWrapper(@NonNull final Data<?> data) {
        return new DataWrapper<Object>(data) {
            @NonNull
            @Override
            public Object get(int position, int flags) {
                return data.get(position, flags);
            }
        };
    }

    private void verifyZeroObserverInteractions() {
        verifyZeroInteractions(mWrapperDataObserver);
        verifyZeroInteractions(mWrapperLoadingObserver);
        verifyZeroInteractions(mWrapperAvailableObserver);
        verifyZeroInteractions(mWrapperErrorObserver);
    }
}
