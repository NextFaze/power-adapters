package com.nextfaze.poweradapters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class AbstractPowerAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private AbstractPowerAdapter mAdapter;

    @Before
    public void setUp() throws Exception {
        mAdapter = spy(AbstractPowerAdapter.class);
    }

    @Test
    public void observerRegistration() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        assertThat(mAdapter.getObserverCount()).isEqualTo(1);
        mAdapter.notifyDataSetChanged();
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void observerUnregistration() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.unregisterDataObserver(observer);
        assertThat(mAdapter.getObserverCount()).isEqualTo(0);
        mAdapter.notifyDataSetChanged();
        verifyZeroInteractions(observer);
    }

    @Test
    public void firstObserverRegisteredWasInvokedForFirstObserverOnly() {
        DataObserver observer = mock(DataObserver.class);
        DataObserver observer2 = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.registerDataObserver(observer2);
        verify(mAdapter).onFirstObserverRegistered();
        verifyNoMoreInteractions(mAdapter);
    }

    @Test
    public void lastObserverUnregisteredWasInvokedForLastObserverOnly() {
        DataObserver observer = mock(DataObserver.class);
        DataObserver observer2 = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.registerDataObserver(observer2);
        mAdapter.unregisterDataObserver(observer);
        mAdapter.unregisterDataObserver(observer2);
        verify(mAdapter).onLastObserverUnregistered();
    }

    // TODO: Each observer notify callback.
}
