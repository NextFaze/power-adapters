package com.nextfaze.poweradapters.data.rx;

import com.nextfaze.poweradapters.Condition;
import com.nextfaze.poweradapters.Observer;
import com.nextfaze.poweradapters.data.FakeData;
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
public final class RxDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Test
    public void loading() {
        FakeData<String> data = new FakeData<>();
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        RxData.loading(data).subscribe(subscriber);
        data.setLoading(true);
        data.setLoading(false);
        subscriber.assertValues(false, true, false);
    }

    @Test
    public void loadingUnsubscribe() {
        FakeData<String> data = new FakeData<>();
        TestSubscriber<Boolean> subscriber = new TestSubscriber<>();
        RxData.loading(data).subscribe(subscriber);
        subscriber.unsubscribe();
        subscriber.assertUnsubscribed();
    }

    @Test
    public void available() {
        FakeData<String> data = new FakeData<>();
        data.setAvailable(0);
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        RxData.available(data).subscribe(subscriber);
        data.setAvailable(5);
        subscriber.assertValues(0, 5);
    }

    @Test
    public void availableUnsubscribe() {
        FakeData<String> data = new FakeData<>();
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        RxData.available(data).subscribe(subscriber);
        subscriber.unsubscribe();
        subscriber.assertUnsubscribed();
    }

    @Test
    public void observableConditionFirstValueIsObserved() {
        Condition condition = RxData.observableCondition(Observable.just(true));
        condition.registerObserver(mock(Observer.class));
        assertThat(condition.eval()).isTrue();
    }

    @Test
    public void observableConditionSubsequentValuesAreObserved() {
        Condition condition = RxData.observableCondition(Observable.just(false, true));
        condition.registerObserver(mock(Observer.class));
        assertThat(condition.eval()).isTrue();
    }

    @Test
    public void observableConditionValuesAreDispatched() {
        Condition condition = RxData.observableCondition(Observable.just(true, false, true, true));
        Observer observer = mock(Observer.class);
        condition.registerObserver(observer);
        verify(observer, times(3)).onChanged();
        verifyNoMoreInteractions(observer);
    }
}