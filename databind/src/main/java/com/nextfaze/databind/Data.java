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
 * the user does not need to necessarily free them manually. It's not recommended that resources be freed immediately
 * upon being hidden, however, because often it is only hidden temporarily. Release them after a delay instead.
 * </p>
 */
public interface Data<T> extends Iterable<T> {

    /** Flag indicating the intent to present information in a user interface. */
    int FLAG_PRESENTATION = 1;

    @NonNull
    T get(int position);

    /**
     * Retrieve the value at the specified position.
     * @param position The position at which to retrieve the value.
     * @param flags Bit field containing flags with extra information about this request for an element.
     * @return The value at the specified position, never {@code null}.
     * @throws RuntimeException If the element is out of bounds or can't be retrieved.
     */
    @NonNull
    T get(int position, int flags);

    /**
     * The number of elements in this data instance.
     * @return The number of elements, always {@code >= 0}.
     */
    int size();

    /** Returns if {@link #size()} {@code == 0}. */
    boolean isEmpty();

    /** If {@code true}, indicates the data is currently loading more elements. */
    boolean isLoading();

    /** If {@code true}, indicates more elements are available to be loaded. */
    boolean isMoreAvailable();

    /** Close this instance. Other methods should not called after this. */
    void close();

    /**
     * Notify this data instance that is currently visibility presented to the user. This cue might be used to start
     * loading elements.
     */
    void notifyShown();

    /**
     * Notify this data instance that is no longer presented to the user. This cue might be used to cancel any active
     * loading operations.
     * <p/>Note that it's common for the data to be in a "hidden" state for a very short interval,
     * such as during a configuration change, so any cancelation should probably occur after a short delay. {@code 3}
     * seconds should suffice.
     */
    void notifyHidden();

    void registerLoadingObserver(@NonNull LoadingObserver loadingObserver);

    void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver);

    void registerErrorObserver(@NonNull ErrorObserver errorObserver);

    void unregisterErrorObserver(@NonNull ErrorObserver errorObserver);

    void registerDataObserver(@NonNull DataObserver dataObserver);

    void unregisterDataObserver(@NonNull DataObserver dataObserver);
}
