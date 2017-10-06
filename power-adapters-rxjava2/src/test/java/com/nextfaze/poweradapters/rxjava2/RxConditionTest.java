package com.nextfaze.poweradapters.rxjava2;

import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.Observer;
import com.nextfaze.poweradapters.ValueCondition;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class RxConditionTest {
    @Test
    public void value() {
        ValueCondition condition = new ValueCondition();
        TestObserver<Boolean> testObserver = RxCondition.value(condition).test();
        condition.set(true);
        testObserver.assertNotTerminated();
        testObserver.assertValues(false, true);
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
