package com.nextfaze.poweradapters

import android.support.annotation.CheckResult

@CheckResult fun PowerAdapter.toListAdapter() = PowerAdapters.toListAdapter(this)