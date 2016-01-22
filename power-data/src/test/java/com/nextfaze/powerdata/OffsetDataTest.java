package com.nextfaze.powerdata;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static java.util.Collections.addAll;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class OffsetDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<String> mData;
    private Data<String> mOffsetData;

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        addAll(mData, "a", "bc", "def", "ghij", "klmno", "pqrstu", "vwxyz12");
        mOffsetData = new OffsetData<>(mData, 5);
    }
}
