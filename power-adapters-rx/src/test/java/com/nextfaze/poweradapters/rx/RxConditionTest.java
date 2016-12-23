package com.nextfaze.poweradapters.rx;

import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.Observer;
import com.nextfaze.poweradapters.ValueCondition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.observers.TestSubscriber;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class RxConditionTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Test
    public void value() {
        ValueCondition condition = new ValueCondition();
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        RxCondition.value(condition).subscribe(subscriber);
        condition.set(true);
        subscriber.assertValues(false, true);
    }

    @Test
    public void observableConditionFirstValueIsObserved() {
        Condition condition = RxCondition.observableCondition(Observable.just(true));
        condition.registerObserver(mock(Observer.class));
        assertThat(condition.eval()).isTrue();
    }

    @Test
    public void observableConditionSubsequentValuesAreObserved() {
        Condition condition = RxCondition.observableCondition(Observable.just(false, true));
        condition.registerObserver(mock(Observer.class));
        assertThat(condition.eval()).isTrue();
    }

    @Test
    public void observableConditionValuesAreDispatched() {
        Condition condition = RxCondition.observableCondition(Observable.just(true, false, true, true));
        Observer observer = mock(Observer.class);
        condition.registerObserver(observer);
        verify(observer, times(3)).onChanged();
        verifyNoMoreInteractions(observer);
    }
}