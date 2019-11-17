package com.nextfaze.poweradapters.sample.cats

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.nextfaze.poweradapters.sample.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cat_view.view.*
import kotlin.properties.Delegates.observable

class CatView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    var cat by observable<Cat?>(null) { _, _, cat ->
        if (cat != null) {
            Picasso.with(context).load(generateImageUri(cat.name)).into(imageView)
            titleView.text = cat.name
            subtitleView.text = cat.country
        }
    }

    init {
        inflate(context, R.layout.cat_view, this)
        useCompatPadding = true
    }
}

private fun generateImageUri(catName: String) =
        Uri.parse("http://thecatapi.com/api/images/get?api_key=MjM0NTc2").buildUpon().appendQueryParameter("cat", catName).build()
