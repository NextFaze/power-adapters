package com.nextfaze.poweradapters

import kotlin.reflect.KProperty

operator fun ValueCondition.getValue(thisRef: Any, property: KProperty<*>): Boolean = get()
operator fun ValueCondition.setValue(thisRef: Any, property: KProperty<*>, value: Boolean) = set(value)