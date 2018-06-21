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

    private Mapper<A> mMapper;

    @Before
    public void setUp() throws Exception {
        mMapper = new MapperBuilder<A>()
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
        Mapper<A> mapper = new MapperBuilder<A>()
                .bind(C.class, mBinder5)
                .bind(A.class, mBinder1, new Predicate<A>() {
                    @Override
                    public boolean apply(A a) {
                        return a.get() == 2;
                    }
                })
                .build();
        assertThat(mapper.getBinder(new D(3), 0)).isNull();
    }

    @Test
    public void absentPredicateEquivalentToAlwaysTrue() {
        Mapper<B> mapper1 = new MapperBuilder<B>()
                .bind(B.class, mBinder3)
                .build();
        Mapper<B> mapper2 = new MapperBuilder<B>()
                .bind(B.class, mBinder3, ALWAYS)
                .build();
        assertThat(mapper1.getBinder(new D(3), 0)).isEqualTo(mBinder3);
        assertThat(mapper2.getBinder(new D(3), 0)).isEqualTo(mBinder3);
    }

    @Test
    public void alwaysFalsePredicateNeverPasses() {
        Mapper<? super C> mapper = new MapperBuilder<C>()
                .bind(C.class, mBinder5, NEVER)
                .build();
        assertThat(mapper.getBinder(new C(7), 0)).isNull();
    }

    class A {

        final int value;

        A(int value) {
            this.value = value;
        }

        int get() {
            return value;
        }
    }

    class B extends A {
        B(int value) {
            super(value);
        }
    }

    class C extends A {
        C(int value) {
            super(value);
        }
    }

    class D extends B {
        D(int value) {
            super(value);
        }
    }

    class E {
    }
}
