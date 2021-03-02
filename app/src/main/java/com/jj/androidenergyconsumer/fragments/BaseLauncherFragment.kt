package com.jj.androidenergyconsumer.fragments

import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.permissions.PermissionManager
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
        context?.apply { unbindFromService(this) }
    }

    protected fun unbindFromService(context: Context) {
        if (serviceBound.compareAndSet(true, false)) {
            context.unbindService(serviceConnection)
        }
    }
}