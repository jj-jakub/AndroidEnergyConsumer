package com.jj.androidenergyconsumer.fragments

import android.content.Context
import android.content.ServiceConnection
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.permissions.PermissionManager
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseLauncherFragment : Fragment() {

    protected val permissionManager = PermissionManager()
    protected var serviceBound = AtomicBoolean(false)
    protected abstract val serviceConnection: ServiceConnection

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