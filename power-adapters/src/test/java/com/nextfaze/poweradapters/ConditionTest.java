package com.nextfaze.poweradapters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public final class ConditionTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private Condition mCondition;

    @Before
    public void setUp() throws Exception {
        mCondition = spy(Condition.class);
    }

    @Test
    public void observerRegistration() {
        Observer observer = mock(Observer.class);
        mCondition.registerObserver(observer);
        assertThat(mCondition.getObserverCount()).isEqualTo(1);
        mCondition.notifyChanged();
        verify(observer).onChanged();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void observerUnregistration() {
        Observer observer = mock(Observer.class);
        mCondition.registerObserver(observer);
        mCondition.unregisterObserver(observer);
        assertThat(mCondition.getObserverCount()).isEqualTo(0);
        mCondition.notifyChanged();
        verifyZeroInteractions(observer);
    }

    @Test(expected = IllegalStateException.class)
    public void doubleRegistrationThrows() {
        Observer observer = mock(Observer.class);
        mCondition.registerObserver(observer);
        mCondition.registerObserver(observer);
    }

    @Test(expected = IllegalStateException.class)
    public void doubleUnregistrationThrows() {
        Observer observer = mock(Observer.class);
        mCondition.registerObserver(observer);
        mCondition.unregisterObserver(observer);
        mCondition.unregisterObserver(observer);
    }

    @Test
    public void firstObserverRegisteredWasInvokedForFirstObserverOnly() {
        Observer observer = mock(Observer.class);
        Observer observer2 = mock(Observer.class);
        mCondition.registerObserver(observer);
        mCondition.registerObserver(observer2);
        verify(mCondition).onFirstObserverRegistered();
    }

    @Test
    public void lastObserverUnregisteredWasInvokedForLastObserverOnly() {
        Observer observer = mock(Observer.class);
        Observer observer2 = mock(Observer.class);
        mCondition.registerObserver(observer);
        mCondition.registerObserver(observer2);
        mCondition.unregisterObserver(observer);
        mCondition.unregisterObserver(observer2);
        verify(mCondition).onLastObserverUnregistered();
    }
}
