package com.nextfaze.poweradapters

infix fun Condition.and(condition: Condition): Condition = Condition.and(this, condition)

infix fun Condition.or(condition: Condition): Condition = Condition.or(this, condition)

infix fun Condition.xor(condition: Condition): Condition = Condition.xor(this, condition)