package com.nextfaze.databind;

import lombok.NonNull;

public interface Data<T> {
    @NonNull
    T get(int position);

    int size();

    boolean isEmpty();

    boolean isLoading();

    void notifyShown();

    void notifyHidden();

    void registerLoadingObserver(@NonNull LoadingObserver loadingObserver);

    void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver);

    void registerErrorObserver(@NonNull ErrorObserver errorObserver);

    void unregisterErrorObserver(@NonNull ErrorObserver errorObserver);

    void registerDataObserver(@NonNull DataObserver dataObserver);

    void unregisterDataObserver(@NonNull DataObserver dataObserver);
}
