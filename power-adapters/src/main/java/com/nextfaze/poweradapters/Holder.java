package com.nextfaze.poweradapters;

/**
 * A "holder" object which can be queried to determine the position of the item in the data set. This
 * is needed because an item may not be rebound if only its position in the data set has changed. Therefore, listener
 * callbacks that require the data set position should invoke the holder.
 */
public interface Holder {
    /**
     * Returns the position of the holder in terms of the latest layout pass.
     * @return Returns the adapter position of the holder in the latest layout pass.
     */
    int getPosition();
}
