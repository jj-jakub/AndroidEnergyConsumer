package com.jj.androidenergyconsumer.domain

import java.text.SimpleDateFormat
import java.util.*

val Any.tag: String get() = this.javaClass.simpleName ifIsEmpty "DefaultTag"

infix fun String.ifIsEmpty(value: String): String = if (isEmpty()) value else this

infix fun String.ifNotEmpty(value: () -> Unit) {
    if (this.isNotEmpty()) value.invoke()
}

fun getDateStringWithMillis(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date())

fun Float.roundAsString(decimals: Int = 2): String = "%.${decimals}f".format(Locale.US, this)