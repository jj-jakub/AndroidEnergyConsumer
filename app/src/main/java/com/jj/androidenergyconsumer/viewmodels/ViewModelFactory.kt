package com.jj.androidenergyconsumer.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jj.androidenergyconsumer.rest.InternetPingCallManager

class ViewModelFactory(private val internetPingCallManager: InternetPingCallManager) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LedControllerViewModel(internetPingCallManager) as T
    }
}
