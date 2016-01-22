package com.nextfaze.powerdata;

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
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class AbstractDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private AbstractData<?> mData;

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
        // Must create spy here, instead of using @Spy annotation, for reasons unknown
        mData = spy(AbstractData.class);
    }

    @Test
    public void availableDefaultUnknown() {
        assertThat(mData.available()).isEqualTo(Data.UNKNOWN);
    }

    @Test
    public void emptyDefaultBasedOnSize() {
        assertThat(mData.isEmpty()).isTrue();
        when(mData.size()).thenReturn(5);
        assertThat(mData.isEmpty()).isFalse();
        when(mData.size()).thenReturn(-3);
        assertThat(mData.isEmpty()).isTrue();
    }

    @Test
    public void dataObserverRegistration() {
        DataObserver dataObserver = mock(DataObserver.class);
        mData.registerDataObserver(dataObserver);
        assertThat(mData.getDataObserverCount()).isEqualTo(1);
        mData.notifyDataChanged();
        verify(dataObserver).onChange();
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void dataObserverUnregistration() {
        DataObserver dataObserver = mock(DataObserver.class);
        mData.registerDataObserver(dataObserver);
        mData.unregisterDataObserver(dataObserver);
        assertThat(mData.getDataObserverCount()).isEqualTo(0);
        mData.notifyDataChanged();
        verifyZeroInteractions(dataObserver);
    }

    @Test
    public void loadingObserverRegistration() {
        LoadingObserver loadingObserver = mock(LoadingObserver.class);
        mData.registerLoadingObserver(loadingObserver);
        assertThat(mData.getLoadingObserverCount()).isEqualTo(1);
        mData.notifyLoadingChanged();
        verify(loadingObserver).onLoadingChange();
        verifyNoMoreInteractions(loadingObserver);
    }

    @Test
    public void loadingObserverUnregistration() {
        LoadingObserver loadingObserver = mock(LoadingObserver.class);
        mData.registerLoadingObserver(loadingObserver);
        mData.unregisterLoadingObserver(loadingObserver);
        assertThat(mData.getLoadingObserverCount()).isEqualTo(0);
        mData.notifyLoadingChanged();
        verifyZeroInteractions(loadingObserver);
    }

    @Test
    public void availableObserverRegistration() {
        AvailableObserver availableObserver = mock(AvailableObserver.class);
        mData.registerAvailableObserver(availableObserver);
        assertThat(mData.getAvailableObserverCount()).isEqualTo(1);
        mData.notifyAvailableChanged();
        verify(availableObserver).onAvailableChange();
        verifyNoMoreInteractions(availableObserver);
    }

    @Test
    public void availableObserverUnregistration() {
        AvailableObserver availableObserver = mock(AvailableObserver.class);
        mData.registerAvailableObserver(availableObserver);
        mData.unregisterAvailableObserver(availableObserver);
        assertThat(mData.getAvailableObserverCount()).isEqualTo(0);
        mData.notifyAvailableChanged();
        verifyZeroInteractions(availableObserver);
    }

    @Test
    public void errorObserverRegistration() {
        ErrorObserver errorObserver = mock(ErrorObserver.class);
        mData.registerErrorObserver(errorObserver);
        assertThat(mData.getErrorObserverCount()).isEqualTo(1);
        RuntimeException exception = new RuntimeException();
        mData.notifyError(exception);
        verify(errorObserver).onError(exception);
        verifyNoMoreInteractions(errorObserver);
    }

    @Test
    public void errorObserverUnregistration() {
        ErrorObserver errorObserver = mock(ErrorObserver.class);
        mData.registerErrorObserver(errorObserver);
        mData.unregisterErrorObserver(errorObserver);
        assertThat(mData.getErrorObserverCount()).isEqualTo(0);
        mData.notifyError(new RuntimeException());
        verifyZeroInteractions(errorObserver);
    }

    @Test
    public void notifyDataChanged() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyDataChanged();
        verify(dataObserver).onChange();
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemChanged() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemChanged(4);
        verify(dataObserver).onItemRangeChanged(4, 1);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemRangeChanged() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemRangeChanged(3, 10);
        verify(dataObserver).onItemRangeChanged(3, 10);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemInserted() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemInserted(3);
        verify(dataObserver).onItemRangeInserted(3, 1);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemRangeInserted() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemRangeInserted(11, 90);
        verify(dataObserver).onItemRangeInserted(11, 90);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemMoved() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemMoved(7, 2);
        verify(dataObserver).onItemRangeMoved(7, 2, 1);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemRangeMoved() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemRangeMoved(10, 15, 5);
        verify(dataObserver).onItemRangeMoved(10, 15, 5);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemRemoved() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemRemoved(1);
        verify(dataObserver).onItemRangeRemoved(1, 1);
        verifyNoMoreInteractions(dataObserver);
    }

    @Test
    public void notifyItemRangeRemoved() {
        DataObserver dataObserver = setUpDataObserver();
        mData.notifyItemRangeRemoved(13, 3);
        verify(dataObserver).onItemRangeRemoved(13, 3);
        verifyNoMoreInteractions(dataObserver);
    }

    @NonNull
    private DataObserver setUpDataObserver() {
        DataObserver dataObserver = mock(DataObserver.class);
        mData.registerDataObserver(dataObserver);
        return dataObserver;
    }

    @Test
    public void firstDataObserverRegisteredCallbackInvoked() {
        DataObserver dataObserver1 = mock(DataObserver.class);
        DataObserver dataObserver2 = mock(DataObserver.class);
        mData.registerDataObserver(dataObserver1);
        mData.registerDataObserver(dataObserver2);
        verify(mData, times(1)).onFirstDataObserverRegistered();
    }

    @Test
    public void lastDataObserverUnregisteredCallbackInvoked() {
        DataObserver dataObserver1 = mock(DataObserver.class);
        DataObserver dataObserver2 = mock(DataObserver.class);
        mData.registerDataObserver(dataObserver1);
        mData.registerDataObserver(dataObserver2);
        mData.unregisterDataObserver(dataObserver1);
        mData.unregisterDataObserver(dataObserver2);
        verify(mData, times(1)).onLastDataObserverUnregistered();
    }

    @Test
    public void iteration() {
        when(mData.size()).thenReturn(3);
        when(mData.get(eq(0), anyInt())).thenReturn("a");
        when(mData.get(eq(1), anyInt())).thenReturn("b");
        when(mData.get(eq(2), anyInt())).thenReturn("c");
        assertThat(mData).containsExactly("a", "b", "c").inOrder();
    }
}
