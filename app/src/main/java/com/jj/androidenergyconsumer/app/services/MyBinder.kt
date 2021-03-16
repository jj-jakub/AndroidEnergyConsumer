package com.jj.androidenergyconsumer.app.services

import android.os.Binder

class MyBinder(service: BaseService): Binder() {

    private val baseService: BaseService = service

    fun getService() = baseService
}