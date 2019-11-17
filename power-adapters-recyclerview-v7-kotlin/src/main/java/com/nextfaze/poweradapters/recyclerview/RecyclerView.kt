package com.nextfaze.poweradapters.recyclerview

import androidx.annotation.CheckResult
import androidx.recyclerview.widget.RecyclerView
import com.nextfaze.poweradapters.PowerAdapter

@CheckResult fun PowerAdapter.toRecyclerAdapter(): RecyclerView.Adapter<*> = RecyclerPowerAdapters.toRecyclerAdapter(this)
