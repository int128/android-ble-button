package org.hidetake.blebutton

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class BleService : Service() {

    var bleContext: BleContext? = null

    // TODO: configurable
    val bleDeviceAddress = "FF:FF:00:00:66:3B"

    class BleServiceBinder(bleContext: BleContext) : Binder() {
        val bleContext = bleContext
    }

    override fun onCreate() {
        Log.d(javaClass.simpleName, "onCreate()")
        bleContext = BleContext(applicationContext, bleDeviceAddress)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(javaClass.simpleName, "onBind()")
        return BleServiceBinder(bleContext!!)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(javaClass.simpleName, "onUnbind()")
        return true
    }

    override fun onRebind(intent: Intent?) {
        Log.d(javaClass.simpleName, "onRebind()")
    }

    override fun onDestroy() {
        Log.d(javaClass.simpleName, "onDestroy()")
        bleContext?.close()
        bleContext = null
    }

}
