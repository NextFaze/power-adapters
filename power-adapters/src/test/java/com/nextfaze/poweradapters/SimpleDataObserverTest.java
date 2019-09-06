package com.nextfaze.poweradapters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public final class SimpleDataObserverTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    private SimpleDataObserver mObserver;

    @Before
    public void setUp() throws Exception {
        mObserver = spy(SimpleDataObserver.class);
    }

    @Test
    public void onItemRangeChangedCallsOnChanged() {
        mObserver.onItemRangeChanged(0, 1, null);
        verify(mObserver).onItemRangeChanged(0, 1, null);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void onItemRangeInsertedCallsOnChanged() {
        mObserver.onItemRangeInserted(0, 1);
        verify(mObserver).onItemRangeInserted(0, 1);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void onItemRangeRemovedCallsOnChanged() {
        mObserver.onItemRangeRemoved(0, 1);
        verify(mObserver).onItemRangeRemoved(0, 1);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void onItemRangeMovedCallsOnChanged() {
        mObserver.onItemRangeMoved(0, 1, 1);
        verify(mObserver).onItemRangeMoved(0, 1, 1);
        verify(mObserver).onChanged();
        verifyNoMoreInteractions(mObserver);
    }
}
