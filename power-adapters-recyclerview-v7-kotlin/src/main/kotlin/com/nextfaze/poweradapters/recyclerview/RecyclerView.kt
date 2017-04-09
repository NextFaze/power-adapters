package com.nextfaze.poweradapters.recyclerview

import android.support.annotation.CheckResult
import android.support.v7.widget.RecyclerView
import com.nextfaze.poweradapters.PowerAdapter

@CheckResult fun PowerAdapter.toRecyclerAdapter(): RecyclerView.Adapter<*> = RecyclerPowerAdapters.toRecyclerAdapter(this)