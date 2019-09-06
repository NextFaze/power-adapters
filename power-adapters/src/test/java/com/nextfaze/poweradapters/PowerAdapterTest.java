package com.nextfaze.poweradapters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public final class PowerAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private PowerAdapter mAdapter;

    @Before
    public void setUp() throws Exception {
        mAdapter = spy(PowerAdapter.class);
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

    @Test(expected = IllegalStateException.class)
    public void doubleRegistrationThrows() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.registerDataObserver(observer);
    }

    @Test(expected = IllegalStateException.class)
    public void doubleUnregistrationThrows() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.unregisterDataObserver(observer);
        mAdapter.unregisterDataObserver(observer);
    }

    @Test
    public void firstObserverRegisteredWasInvokedForFirstObserverOnly() {
        DataObserver observer = mock(DataObserver.class);
        DataObserver observer2 = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.registerDataObserver(observer2);
        verify(mAdapter).registerDataObserver(observer);
        verify(mAdapter).registerDataObserver(observer2);
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

    @Test
    public void notifyDataSetChangedInvokesObservers() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyDataSetChanged();
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notifyChangeInvokesObservers() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeChanged(3, 5, null);
        verify(observer).onItemRangeChanged(3, 5, null);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notifyInsertInvokesObservers() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeInserted(15, 1);
        verify(observer).onItemRangeInserted(15, 1);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notifyRemoveInvokesObservers() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeRemoved(9, 2);
        verify(observer).onItemRangeRemoved(9, 2);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notifyMoveInvokesObservers() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeMoved(3, 10, 2);
        verify(observer).onItemRangeMoved(3, 10, 2);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notifyZeroChangeDoesNothing() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeChanged(3, 0, null);
        verifyZeroInteractions(observer);
    }

    @Test
    public void notifyZeroInsertDoesNothing() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeInserted(9, 0);
        verifyZeroInteractions(observer);
    }

    @Test
    public void notifyZeroRemoveDoesNothing() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeRemoved(12, 0);
        verifyZeroInteractions(observer);
    }

    @Test
    public void notifyZeroMoveDoesNothing() {
        DataObserver observer = mock(DataObserver.class);
        mAdapter.registerDataObserver(observer);
        mAdapter.notifyItemRangeMoved(3, 7, 0);
        verifyZeroInteractions(observer);
    }
}
