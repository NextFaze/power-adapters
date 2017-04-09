package com.nextfaze.poweradapters.sample;

import android.content.Context;
import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;

import static com.nextfaze.poweradapters.sample.Utils.loadNextButton;
import static com.nextfaze.poweradapters.sample.Utils.loadingIndicator;

final class Cats {

    @NonNull
    private static final Binder<Cat, CatView> sCatBinder = Binder.create(R.layout.cat_binder_item, ((container, cat, catView, holder) -> {
        catView.setCat(cat);
    }));

    @NonNull
    static CatData createData(@NonNull Context context) {
        return new CatData(context);
    }

    @NonNull
    static PowerAdapter createAdapter(@NonNull Data<? extends Cat> data) {
        return new DataBindingAdapter<>(sCatBinder, data)
                .append(loadingIndicator(data), loadNextButton(data));
    }
}
