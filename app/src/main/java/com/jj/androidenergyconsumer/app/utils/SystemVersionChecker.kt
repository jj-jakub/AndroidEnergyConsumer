package com.jj.androidenergyconsumer.app.utils

import android.os.Build

class SystemVersionChecker {
    fun isAndroid10OrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    fun isAndroid8OrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    fun isAndroid6OrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}