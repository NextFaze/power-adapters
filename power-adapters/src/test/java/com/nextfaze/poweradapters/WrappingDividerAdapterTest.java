package com.nextfaze.poweradapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import lombok.NonNull;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static com.nextfaze.poweradapters.DividerAdapterBuilder.EmptyPolicy.*;
import static com.nextfaze.poweradapters.internal.NotificationType.COARSE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public final class WrappingDividerAdapterTest {

    @LayoutRes
    private static final int RESOURCE = 0;

    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Mock
    private DataObserver mObserver;

    private FakeAdapter mFakeAdapter;
    private PowerAdapter mDividerAdapter;

    @Nullable
    private VerifyingAdapterObserver mVerifyingObserver;

    @After
    public void tearDown() throws Exception {
        if (mVerifyingObserver != null) {
            mVerifyingObserver.assertItemCountConsistent();
        }
    }

    private void configure(int count) {
        configure(count, SHOW_NOTHING);
    }

    private void configure(int count, @NonNull DividerAdapterBuilder.EmptyPolicy emptyPolicy) {
        mFakeAdapter = new FakeAdapter(count);
        mDividerAdapter = new DividerAdapterBuilder()
                .innerResource(RESOURCE)
                .outerResource(RESOURCE)
                .emptyPolicy(emptyPolicy)
                .build(mFakeAdapter);
        mVerifyingObserver = new VerifyingAdapterObserver(mDividerAdapter);
        mDividerAdapter.registerDataObserver(mVerifyingObserver);
        mDividerAdapter.registerDataObserver(mObserver);
    }

    // TODO: Check newView/bindView maps to correct divider items.

    // TODO: Check isEnabled/getItemViewType maps to correct divider items.

    @Test
    public void itemCountGreaterThanOne() {
        configure(5, SHOW_LEADING);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(5);
    }

    @Test
    public void itemCountOne() {
        configure(1, SHOW_LEADING);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(1);
    }

    @Test
    public void itemCountZeroShowLeading() {
        configure(0, SHOW_LEADING);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(1);
    }

    @Test
    public void itemCountZeroShowTrailing() {
        configure(0, SHOW_TRAILING);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(1);
    }

    @Test
    public void itemCountZeroShowLeadingAndTrailing() {
        configure(0, SHOW_LEADING_AND_TRAILING);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(2);
    }

    @Test
    public void itemCountConsistentAfterCoarseNotification() {
        configure(10);
        mFakeAdapter.setNotificationType(COARSE);
        mFakeAdapter.insert(0, 10);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(20);
        verify(mObserver).onChanged();
    }

    @Test
    public void removeToEmptyShowNothing() {
        configure(5, SHOW_NOTHING);
        mFakeAdapter.clear();
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(0);
        verify(mObserver).onItemRangeRemoved(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeToEmptyShowLeading() {
        configure(5, SHOW_LEADING);
        mFakeAdapter.clear();
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(1);
        verify(mObserver).onItemRangeRemoved(0, 5);
        verify(mObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeToEmptyShowTrailing() {
        configure(5, SHOW_TRAILING);
        mFakeAdapter.clear();
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(1);
        verify(mObserver).onItemRangeRemoved(0, 5);
        verify(mObserver).onItemRangeInserted(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeToEmptyShowLeadingAndTrailing() {
        configure(5, SHOW_LEADING_AND_TRAILING);
        mFakeAdapter.clear();
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(2);
        verify(mObserver).onItemRangeRemoved(0, 5);
        verify(mObserver).onItemRangeInserted(0, 1);
        verify(mObserver).onItemRangeInserted(1, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeToNonEmptyFromStartRange() {
        configure(15);
        mFakeAdapter.remove(0, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(10);
        verify(mObserver).onItemRangeRemoved(0, 5);
        verify(mObserver).onItemRangeChanged(0, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeToNonEmptyFromEndRange() {
        configure(15);
        mFakeAdapter.remove(10, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(10);
        verify(mObserver).onItemRangeRemoved(10, 5);
        verify(mObserver).onItemRangeChanged(9, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void removeToNonEmptyFromMidRange() {
        configure(15);
        mFakeAdapter.remove(5, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(10);
        verify(mObserver).onItemRangeRemoved(5, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmptyShowNothing() {
        configure(0, SHOW_NOTHING);
        mFakeAdapter.insert(0, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(5);
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmptyShowLeading() {
        configure(0, SHOW_LEADING);
        mFakeAdapter.insert(0, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(5);
        verify(mObserver).onItemRangeRemoved(0, 1);
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmptyShowTrailing() {
        configure(0, SHOW_TRAILING);
        mFakeAdapter.insert(0, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(5);
        verify(mObserver).onItemRangeRemoved(0, 1);
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromEmptyShowLeadingAndTrailing() {
        configure(0, SHOW_LEADING_AND_TRAILING);
        mFakeAdapter.insert(0, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(5);
        verify(mObserver).onItemRangeRemoved(0, 1);
        verify(mObserver).onItemRangeRemoved(1, 1);
        verify(mObserver).onItemRangeInserted(0, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromNonEmptyToStartRange() {
        configure(10);
        mFakeAdapter.insert(0, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(15);
        verify(mObserver).onItemRangeInserted(0, 5);
        verify(mObserver).onItemRangeChanged(5, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromNonEmptyToEndRange() {
        configure(10);
        mFakeAdapter.insert(10, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(15);
        verify(mObserver).onItemRangeInserted(10, 5);
        verify(mObserver).onItemRangeChanged(9, 1);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void insertFromNonEmptyToMidRange() {
        configure(10);
        mFakeAdapter.insert(5, 5);
        assertThat(mDividerAdapter.getItemCount()).isEqualTo(15);
        verify(mObserver).onItemRangeInserted(5, 5);
        verifyNoMoreInteractions(mObserver);
    }

    @Test
    public void itemCountIsConsistentWhenChildIssuesInsertionUponRegisterObserverShowLeading() {
        PowerAdapter adapter = new FakeAdapter(5).append(new FakeAdapter(5));
        PowerAdapter dividerAdapter = new DividerAdapterBuilder()
                .innerResource(RESOURCE)
                .outerResource(RESOURCE)
                .emptyPolicy(SHOW_LEADING)
                .build(adapter);
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(dividerAdapter);
        dividerAdapter.registerDataObserver(verifyingObserver);
        verifyingObserver.assertItemCountConsistent();
    }

    @Test
    public void itemCountIsConsistentWhenChildIssuesInsertionUponRegisterObserverShowTrailing() {
        PowerAdapter adapter = new FakeAdapter(5).append(new FakeAdapter(5));
        PowerAdapter dividerAdapter = new DividerAdapterBuilder()
                .innerResource(RESOURCE)
                .outerResource(RESOURCE)
                .emptyPolicy(SHOW_TRAILING)
                .build(adapter);
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(dividerAdapter);
        dividerAdapter.registerDataObserver(verifyingObserver);
        verifyingObserver.assertItemCountConsistent();
    }

    @Test
    public void itemCountIsConsistentWhenChildIssuesInsertionUponRegisterObserverShowLeadingAndTrailing() {
        PowerAdapter adapter = new FakeAdapter(5).append(new FakeAdapter(5));
        PowerAdapter dividerAdapter = new DividerAdapterBuilder()
                .innerResource(RESOURCE)
                .outerResource(RESOURCE)
                .emptyPolicy(SHOW_LEADING_AND_TRAILING)
                .build(adapter);
        VerifyingAdapterObserver verifyingObserver = new VerifyingAdapterObserver(dividerAdapter);
        dividerAdapter.registerDataObserver(verifyingObserver);
        verifyingObserver.assertItemCountConsistent();
    }

    // TODO: Check the above again with certain dividers absent, eg. leading + trailing, but no inner.
}