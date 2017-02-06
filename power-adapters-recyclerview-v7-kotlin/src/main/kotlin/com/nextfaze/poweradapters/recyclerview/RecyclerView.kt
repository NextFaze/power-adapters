package com.nextfaze.poweradapters.recyclerview

import android.support.v7.widget.RecyclerView
import com.nextfaze.poweradapters.PowerAdapter

fun PowerAdapter.toRecyclerAdapter(): RecyclerView.Adapter<*> = RecyclerPowerAdapters.toRecyclerAdapter(this)