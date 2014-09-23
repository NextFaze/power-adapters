package com.nextfaze.databind;

import lombok.NonNull;

/**
 * Provides access to an asynchronously loaded data source.
 * <p>
 * A {@code Data} instance may be in a loading state, which it broadcasts to interested observers so they can present
 * loading indicators.
 * </p>
 * <p>
 * It also broadcasts errors it encounters while loading.
 * </p>
 * <p>
 * It may also be in a shown or hidden state. When hidden, the data may opt to release any internal resources. This way
 * the user does not need to necessarily free them manually. It's not recommended that resources be
 * free immediately upon being hidden, however, because often it is only hidden temporarily. Release them after a delay
 * instead.
 * </p>
 */
public interface Data<T> {
    @NonNull
    T get(int position);

    int size();

    boolean isEmpty();

    boolean isLoading();

    void close();

    void notifyShown();

    void notifyHidden();

    void registerLoadingObserver(@NonNull LoadingObserver loadingObserver);

    void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver);

    void registerErrorObserver(@NonNull ErrorObserver errorObserver);

    void unregisterErrorObserver(@NonNull ErrorObserver errorObserver);

    void registerDataObserver(@NonNull DataObserver dataObserver);

    void unregisterDataObserver(@NonNull DataObserver dataObserver);
}
