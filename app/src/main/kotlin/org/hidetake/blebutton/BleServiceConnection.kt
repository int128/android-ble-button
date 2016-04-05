package org.hidetake.blebutton

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log

class BleServiceConnection() : ServiceConnection {

    class BleServiceBinder(service: BleService) : Binder() {
        val service = service
    }

    private var _service: BleService? = null

    val service: BleService?
        get() = _service

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        Log.d("BleServiceConnection", "onServiceConnected()")
        if (binder is BleServiceBinder) {
            _service = binder.service
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("BleServiceConnection", "onServiceDisconnected()")
        _service = null
    }

}
