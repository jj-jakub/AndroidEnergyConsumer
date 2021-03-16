package com.jj.androidenergyconsumer.app.fragments

import android.content.ServiceConnection
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseLauncherFragment : Fragment() {

    protected val permissionManager = PermissionManager()
    protected var serviceBound = AtomicBoolean(false)
    protected abstract val serviceConnection: ServiceConnection
    protected abstract val activityTitle: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = activityTitle
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindFromService()
    }

    protected fun unbindFromService() {
        if (serviceBound.compareAndSet(true, false)) {
            context?.unbindService(serviceConnection)
        }
    }
}