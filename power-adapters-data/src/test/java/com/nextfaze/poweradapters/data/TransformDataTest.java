package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;

import com.nextfaze.poweradapters.data.test.FakeData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class TransformDataTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private FakeData<String> mData;

    @Before
    public void setUp() throws Exception {
        mData = new FakeData<>();
        mData.insert(0, "a", "bc", "def", "ghij", "klmno", "pqrstu", "vwxyz12");
    }

    @Test
    public void transform() {
        Data<Integer> transformed = new TransformData<>(mData, new Function<String, Integer>() {
            @NonNull
            @Override
            public Integer apply(@NonNull String s) {
                return s.length();
            }
        });
        assertThat(transformed).containsExactly(1, 2, 3, 4, 5, 6, 7).inOrder();
    }
}
