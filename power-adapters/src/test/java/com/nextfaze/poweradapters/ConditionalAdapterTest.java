package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;

import com.nextfaze.poweradapters.test.FakeAdapter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static com.nextfaze.poweradapters.AdapterTestUtils.holder;
import static com.nextfaze.poweradapters.ArgumentMatchers.holderWithPosition;
import static com.nextfaze.poweradapters.Condition.always;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public final class ConditionalAdapterTest {

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private FakeAdapter mFakeAdapter;
    private ConditionalAdapter mConditionalAdapter;
    private android.view.ViewGroup mParent;
    private View mItemView;

    @Mock
    private Container mContainer;

    private VerifyingAdapterObserver mVerifyingObserver;

    @Before
    public void setUp() throws Exception {
        mParent = new FrameLayout(RuntimeEnvironment.application);
        mItemView = new View(RuntimeEnvironment.application);
    }

    private void setCondition(@NonNull Condition condition) {
        mFakeAdapter = spy(new FakeAdapter(10));
        mConditionalAdapter = new ConditionalAdapter(mFakeAdapter, condition);
        mVerifyingObserver = new VerifyingAdapterObserver(mConditionalAdapter);
        mConditionalAdapter.registerDataObserver(mVerifyingObserver);
        mConditionalAdapter.registerDataObserver(mObserver);
    }

    private void assertItemCountConsistent() {
        mVerifyingObserver.assertItemCountConsistent();
    }

    @Test
    public void itemCountIsZeroWhenFalse() {
        setCondition(Condition.never());
        assertThat(mConditionalAdapter.getItemCount()).isEqualTo(0);
        assertItemCountConsistent();
    }

    @Test
    public void itemCountIsNonZeroWhenTrue() {
        setCondition(always());
        assertThat(mConditionalAdapter.getItemCount()).isEqualTo(10);
        assertItemCountConsistent();
    }

    @Test
    public void parentRegistersWithConditionUponFirstExternalObserverRegistering() {
        Condition condition = mock(Condition.class);
        ConditionalAdapter conditionalAdapter = new ConditionalAdapter(mock(PowerAdapter.class), condition);
        DataObserver observer = mock(DataObserver.class);
        conditionalAdapter.registerDataObserver(observer);
        verify(condition).registerObserver(any(Observer.class));
    }

    @Test
    public void parentUnregistersFromConditionUponLastExternalObserverUnregistering() {
        Condition condition = mock(Condition.class);
        ConditionalAdapter conditionalAdapter = new ConditionalAdapter(mock(PowerAdapter.class), condition);
        VerifyingAdapterObserver observer = new VerifyingAdapterObserver(conditionalAdapter);
        conditionalAdapter.registerDataObserver(observer);
        observer.assertItemCountConsistent();
        conditionalAdapter.unregisterDataObserver(observer);
        observer.assertItemCountConsistent();
        ArgumentCaptor<Observer> captor = ArgumentCaptor.forClass(Observer.class);
        verify(condition).registerObserver(captor.capture());
        verify(condition).unregisterObserver(eq(captor.getValue()));
    }

    @Test
    public void parentRegistersWithChildOnlyWhileConditionIsTrue() {
        ValueCondition condition = new ValueCondition();
        PowerAdapter childAdapter = mock(PowerAdapter.class);
        ConditionalAdapter conditionalAdapter = new ConditionalAdapter(childAdapter, condition);
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(conditionalAdapter);
        conditionalAdapter.registerDataObserver(verifyingObserver);
        verify(childAdapter, never()).registerDataObserver(any(DataObserver.class));
        condition.set(true);
        verify(childAdapter).registerDataObserver(any(DataObserver.class));
        verifyingObserver.assertItemCountConsistent();
    }

    @Test
    public void conditionIsNotEvaluatedUponConstruction() {
        Condition condition = mock(Condition.class);
        new ConditionalAdapter(mock(PowerAdapter.class), condition);
        verify(condition, never()).eval();
    }

    @Test
    public void conditionIsEvaluatedUponFirstObserverRegistered() {
        Condition condition = mock(Condition.class);
        ConditionalAdapter conditionalAdapter = new ConditionalAdapter(mock(PowerAdapter.class), condition);
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(conditionalAdapter);
        conditionalAdapter.registerDataObserver(verifyingObserver);
        verify(condition).eval();
        verifyingObserver.assertItemCountConsistent();
    }

    @Test
    public void parentIssuesInsertionWhenConditionBecomesTrue() {
        ValueCondition condition = new ValueCondition();
        setCondition(condition);
        condition.set(true);
        verify(mObserver).onItemRangeInserted(0, 10);
        verifyNoMoreInteractions(mObserver);
        assertItemCountConsistent();
    }

    @Test
    public void parentIssuesRemovalWhenConditionBecomesFalse() {
        ValueCondition condition = new ValueCondition(true);
        setCondition(condition);
        condition.set(false);
        verify(mObserver).onItemRangeRemoved(0, 10);
        verifyNoMoreInteractions(mObserver);
        assertItemCountConsistent();
    }

    @Test
    public void parentDelegatesNewViewToChildWhileConditionIsTrue() {
        setCondition(always());
        Object viewType = mConditionalAdapter.getItemViewType(9);
        mConditionalAdapter.newView(mParent, viewType);
        verify(mFakeAdapter).newView(mParent, viewType);
        assertItemCountConsistent();
    }

    @Test(expected = Throwable.class)
    public void parentThrowsFromNewViewWhileConditionIsFalse() {
        setCondition(Condition.never());
        mConditionalAdapter.newView(mParent, new Object());
        assertItemCountConsistent();
    }

    @Test
    public void parentDelegatesBindViewToChildWhileConditionIsTrue() {
        setCondition(always());
        mConditionalAdapter.bindView(mContainer, mItemView, holder(2));
        verify(mFakeAdapter).bindView(eq(mContainer), eq(mItemView), argThat(holderWithPosition(2)));
        assertItemCountConsistent();
    }

    @Test(expected = Throwable.class)
    public void parentThrowsFromBindViewWhileConditionIsFalse() {
        setCondition(Condition.never());
        mConditionalAdapter.bindView(mContainer, mItemView, holder(5));
        assertItemCountConsistent();
    }

    @Test(expected = Throwable.class)
    public void parentThrowsFromGetItemViewTypeWhileConditionIsFalse() {
        setCondition(Condition.never());
        mConditionalAdapter.getItemViewType(5);
        assertItemCountConsistent();
    }

    @Test(expected = Throwable.class)
    public void parentThrowsFromGetItemIdWhileConditionIsFalse() {
        setCondition(Condition.never());
        mConditionalAdapter.getItemId(2);
        assertItemCountConsistent();
    }

    @Test(expected = Throwable.class)
    public void parentThrowsFromIsEnabledWhileConditionIsFalse() {
        setCondition(Condition.never());
        mConditionalAdapter.isEnabled(6);
        assertItemCountConsistent();
    }

    @Test
    public void parentForwardsStableIds() {
        setCondition(always());
        mConditionalAdapter.hasStableIds();
        verify(mFakeAdapter).hasStableIds();
        assertItemCountConsistent();
    }

    @Test
    public void childChangeIsForwardedWhileConditionIsTrue() {
        setCondition(always());
        mFakeAdapter.change(3, 5);
        verify(mObserver).onItemRangeChanged(3, 5);
        assertItemCountConsistent();
    }

    @Test
    public void childInsertionIsForwardedWhileConditionIsTrue() {
        setCondition(always());
        mFakeAdapter.insert(2, 9);
        verify(mObserver).onItemRangeInserted(2, 9);
        assertItemCountConsistent();
    }

    @Test
    public void childRemovalIsForwardedWhileConditionIsTrue() {
        setCondition(always());
        mFakeAdapter.remove(9, 1);
        verify(mObserver).onItemRangeRemoved(9, 1);
        assertItemCountConsistent();
    }

    @Test
    public void childMoveIsForwardedWhileConditionIsTrue() {
        setCondition(always());
        mFakeAdapter.move(3, 5, 1);
        verify(mObserver).onItemRangeMoved(3, 5, 1);
        assertItemCountConsistent();
    }

    @Test
    public void childChangeIsSuppressedWhileConditionIsFalse() {
        setCondition(Condition.never());
        mFakeAdapter.change(3, 5);
        verifyZeroInteractions(mObserver);
        assertItemCountConsistent();
    }

    @Test
    public void childInsertionIsSuppressedWhileConditionIsFalse() {
        setCondition(Condition.never());
        mFakeAdapter.insert(2, 9);
        verifyZeroInteractions(mObserver);
        assertItemCountConsistent();
    }

    @Test
    public void childInsertionUponFirstRegistrationIsNotDuplicated() {
        PowerAdapter fakeAdapter = new FakeAdapter(10);
        ConditionalAdapter nestedConditionalAdapter = new ConditionalAdapter(fakeAdapter, always());
        ConditionalAdapter conditionalAdapter = new ConditionalAdapter(nestedConditionalAdapter, always());
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(conditionalAdapter);
        conditionalAdapter.registerDataObserver(verifyingObserver);
        conditionalAdapter.registerDataObserver(mObserver);
        assertThat(conditionalAdapter.getItemCount()).isEqualTo(10);
        verifyNoMoreInteractions(mObserver);
        verifyingObserver.assertItemCountConsistent();
    }

    @Test
    public void childRemovalIsSuppressedWhileConditionIsFalse() {
        setCondition(Condition.never());
        mFakeAdapter.remove(9, 1);
        verifyZeroInteractions(mObserver);
        assertItemCountConsistent();
    }

    @Test
    public void childMoveIsSuppressedWhileConditionIsFalse() {
        setCondition(Condition.never());
        mFakeAdapter.move(3, 5, 1);
        verifyZeroInteractions(mObserver);
        assertItemCountConsistent();
    }
}
