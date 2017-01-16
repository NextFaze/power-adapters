package com.nextfaze.poweradapters.sample;

import android.content.Context;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import com.nextfaze.poweradapters.binding.Binder;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataBindingAdapter;
import lombok.NonNull;

import static com.nextfaze.poweradapters.sample.Utils.loadNextButton;
import static com.nextfaze.poweradapters.sample.Utils.loadingIndicator;

final class Cats {

    @NonNull
    private static final Binder<Cat, CatView> sCatBinder = new AbstractBinder<Cat, CatView>(R.layout.cat_binder_item) {
        @Override
        public void bindView(@NonNull Cat cat, @NonNull CatView v, @NonNull Holder holder) {
            v.setCat(cat);
        }
    };

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
