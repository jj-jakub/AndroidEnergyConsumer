package com.jj.androidenergyconsumer.services

import android.os.Binder

class MyBinder(service: CalculationsService): Binder() {

    private val calculationsService: CalculationsService = service

    fun getCalculationsService() = calculationsService
}