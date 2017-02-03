package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.addAll;
import static org.mockito.Mockito.inOrder;

final class AdapterVerifier {

    /** Set of mocked root and child adapters. */
    @NonNull
    private final Set<PowerAdapter> mMockSubAdapters = new HashSet<>();

    /** Set of get calls to be verified as passing the correct position arg. */
    @NonNull
    final Set<GetCall> mGetCalls = new HashSet<>();

    /** List of verifications to be performed in order. */
    @NonNull
    private final List<Check> mChecks = new ArrayList<>();

    AdapterVerifier(@NonNull GetCall... getCalls) {
        addAll(mGetCalls, getCalls);
    }

    @NonNull
    static AdapterVerifier verifySubAdapterAllGetCalls() {
        return verifySubAdapterCalls(GetCall.values());
    }

    @NonNull
    static AdapterVerifier verifySubAdapterCalls(@NonNull GetCall... getCalls) {
        return new AdapterVerifier(getCalls);
    }

    /** Verify that the specified mocked sub adapter was invoked with the specified position arg. */
    @NonNull
    AdapterVerifier check(@NonNull final PowerAdapter mockAdapter, final int position) {
        mMockSubAdapters.add(mockAdapter);
        mChecks.add(new Check() {
            @Override
            public void run(@NonNull InOrder inOrder) {
                for (GetCall call : mGetCalls) {
                    call.get(inOrder.verify(mockAdapter), position);
                }
            }
        });
        return this;
    }

    @NonNull
    AdapterVerifier checkRange(@NonNull PowerAdapter mockAdapter, int positionStart, int itemCount) {
        for (int i = positionStart; i < positionStart + itemCount; i++) {
            check(mockAdapter, i);
        }
        return this;
    }

    /** Must be called at the end to perform the verification. */
    void verify(@NonNull PowerAdapter parentAdapter) {
        checkState(!mGetCalls.isEmpty(), "Must specify at least one " + GetCall.class.getSimpleName());
        checkState(!mMockSubAdapters.isEmpty(), "Must specify at least one mock sub adapter");
        // Parent adapter item count must match number of checks,
        // since we're verifying each position maps to the right sub adapter.
        assertThat(parentAdapter.getItemCount()).isEqualTo(mChecks.size());
        InOrder inOrder = inOrder(mMockSubAdapters.toArray());
        for (int i = 0; i < mChecks.size(); i++) {
            for (GetCall call : mGetCalls) {
                call.get(parentAdapter, i);
            }
        }
        for (Check check : mChecks) {
            check.run(inOrder);
        }
        inOrder.verifyNoMoreInteractions();
    }

    interface Check {
        void run(@NonNull InOrder inOrder);
    }
}
