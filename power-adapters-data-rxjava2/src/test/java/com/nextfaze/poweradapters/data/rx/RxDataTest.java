package com.nextfaze.poweradapters.data.rx;

import com.nextfaze.poweradapters.data.FakeData;
import com.nextfaze.poweradapters.data.rxjava2.BuildConfig;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class RxDataTest {
    @Test
    public void loading() {
        FakeData<String> data = new FakeData<>();
        TestObserver<Boolean> testObserver = RxData.loading(data).test();
        data.setLoading(true);
        data.setLoading(false);
        testObserver.assertNotTerminated();
        testObserver.assertValues(false, true, false);
    }

    @Test
    public void available() {
        FakeData<String> data = new FakeData<>();
        data.setAvailable(0);
        TestObserver<Integer> testObserver = RxData.available(data).test();
        data.setAvailable(5);
        testObserver.assertNotTerminated();
        testObserver.assertValues(0, 5);
    }
}
