package androidx.viewpager.widget;

import android.database.DataSetObserver;

import androidx.annotation.Nullable;

/**
 * Hacks into {@link PagerAdapter} to ensure {@link #registerDataSetObserver} gets called when {@link ViewPager} sets
 * its own back-channel observer via {@link #setViewPagerObserver(DataSetObserver)}.
 * @hide Not intended for public use.
 */
public abstract class PagerAdapterFixed extends PagerAdapter {

    @Nullable
    private DataSetObserver mRegisteredObserver;

    @Override
    void setViewPagerObserver(DataSetObserver observer) {
        super.setViewPagerObserver(observer);
        if (observer != mRegisteredObserver) {
            if (mRegisteredObserver != null) {
                unregisterDataSetObserver(mRegisteredObserver);
            }
            mRegisteredObserver = observer;
            if (mRegisteredObserver != null) {
                registerDataSetObserver(mRegisteredObserver);
            }
        }
    }
}
