package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.util.concurrent.RoboExecutorService;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class ArrayDataTest {

    private RoboExecutorService mExecutor;

    @Before
    public void setUp() throws Exception {
        mExecutor = new RoboExecutorService();
    }

    @Test
    public void changeAndRemovalComboNotificationsMatchReportedSize() throws InterruptedException {
        List<String> initialContents = asList("foo", "bar", "baz");
        List<String> minusOneElement = asList("foo", "baz");

        final AtomicReference<List<String>> ref = new AtomicReference<>(initialContents);
        ArrayData<String> data = new ArrayData<String>(mExecutor) {
            @NonNull
            @Override
            protected List<String> load() throws Throwable {
                return ref.get();
            }
        };
        TestObserver<String> testObserver = new TestObserver<>(data);
        data.registerDataObserver(testObserver);

        testObserver.awaitContent(initialContents);
        ref.set(minusOneElement);
        data.refresh();

        testObserver.awaitContent(minusOneElement);
        testObserver.assertNotificationsConsistent();
    }
}

