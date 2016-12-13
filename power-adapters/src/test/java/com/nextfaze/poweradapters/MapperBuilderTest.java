package com.nextfaze.poweradapters;

import android.widget.TextView;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.binding.Mapper;
import com.nextfaze.poweradapters.binding.MapperBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class MapperBuilderTest {

    private static final Predicate<Object> ALWAYS = new Predicate<Object>() {
        @Override
        public boolean apply(Object o) {
            return true;
        }
    };
    private static final Predicate<Object> NEVER = new Predicate<Object>() {
        @Override
        public boolean apply(Object o) {
            return false;
        }
    };

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private Binder<A, TextView> mBinder1;

    @Mock
    private Binder<A, TextView> mBinder2;

    @Mock
    private Binder<B, TextView> mBinder3;

    @Mock
    private Binder<B, TextView> mBinder4;

    @Mock
    private Binder<C, TextView> mBinder5;

    @Mock
    private Binder<C, TextView> mBinder6;

    private Mapper mMapper;

    @Before
    public void setUp() throws Exception {
        mMapper = new MapperBuilder()
                .bind(C.class, mBinder5)
                .bind(A.class, mBinder1, new Predicate<A>() {
                    @Override
                    public boolean apply(A a) {
                        return a.get() == 2;
                    }
                })
                .bind(A.class, mBinder2)
                .build();
    }

    @Test
    public void derivedItemResolvesToFirstAssignableBinding() {
        assertThat(mMapper.getBinder(new D(0), 0)).isEqualTo(mBinder2);
    }

    @Test
    public void derivedItemResolvesToFirstAssignableBindingThatMatchesPredicate() {
        assertThat(mMapper.getBinder(new D(2), 0)).isEqualTo(mBinder1);
    }

    @Test
    public void nonBoundItemResolvesToNull() {
        assertThat(mMapper.getBinder(new E(), 0)).isNull();
    }

    @Test
    public void absentPredicateEquivalentToAlwaysTrue() {
        Mapper mapper1 = new MapperBuilder()
                .bind(B.class, mBinder3)
                .build();
        Mapper mapper2 = new MapperBuilder()
                .bind(B.class, mBinder3, ALWAYS)
                .build();
        assertThat(mapper1.getBinder(new D(3), 0)).isEqualTo(mBinder3);
        assertThat(mapper2.getBinder(new D(3), 0)).isEqualTo(mBinder3);
    }

    @Test
    public void alwaysFalsePredicateNeverPasses() {
        Mapper mapper = new MapperBuilder()
                .bind(C.class, mBinder5, NEVER)
                .build();
        assertThat(mapper.getBinder(new D(7), 0)).isNull();
    }

    interface A {
        int get();
    }

    interface B extends A {
    }

    interface C extends A {
    }

    class D implements B {
        final int value;

        D(int value) {
            this.value = value;
        }

        @Override
        public int get() {
            return value;
        }
    }

    class E {
    }
}
