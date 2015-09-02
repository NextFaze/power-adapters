package com.nextfaze.poweradapters;

/**
 * A "holder" object which can be queried to determine the position of the item in the data set. This
 * is needed because an item may not be rebound if only its position in the data set has changed. Therefore, listener
 * callbacks that require the data set position should invoke the holder. If a data set change notification has been
 * recently dispatched, the return value may be {@link Holder#NO_POSITION}.
 */
public interface Holder {

    int NO_POSITION = -1;

    int getPosition();
}
