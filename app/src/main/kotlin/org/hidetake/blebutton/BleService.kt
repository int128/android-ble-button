package org.hidetake.blebutton

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

class BleService : Service() {

    private var bleContext: BleContext? = null

    val bleDeviceAddress: String?
        get() = bleContext?.bleDeviceAddress

    val bleState: BleState?
        get() = bleContext?.bleState

    fun immediateAlert() = bleContext?.immediateAlert()

    override fun onCreate() {
        Log.d("BleService", "onCreate()")

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(onPreferenceChanged)

        bleContext = BleContext(applicationContext, preferences.getString(MyPreference.device_address.name, ""))

        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.registerReceiver(onBleButtonPressed, BleState.intentFilter(BleState.BUTTON_PRESSED))

        bleContext!!.connect()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("BleService", "onBind()")
        return BleServiceConnection.BleServiceBinder(this)
    }

    private val onBleButtonPressed = LambdaBroadcastReceiver { context, intent ->
        Log.d("BleService", "onBleButtonPressed()")

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val ringtone = preferences.getString(MyPreference.notifications_ble_button_ringtone.name, "")
        val vibrate = preferences.getBoolean(MyPreference.notifications_ble_button_vibrate.name, true)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Notification.Builder(applicationContext)
                .setSmallIcon(R.drawable.bluetooth_notification)
                .setColor(applicationContext.getColor(R.color.colorPrimary))
                .setContentTitle("BLE button is pressed")
                .setContentText(bleContext?.bleDeviceAddress)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSound(Uri.parse(ringtone))
                .setVibrate(if (vibrate) longArrayOf(100, 100, 100, 100) else null)
                .setAutoCancel(true)
                .build()
        notificationManager.notify(1, notification)
    }

    private val onPreferenceChanged = SharedPreferences.OnSharedPreferenceChangeListener { preferences, key ->
        when (key) {
            "device_address" -> {
                Log.d("BleService", "onPreferenceChanged(device_address)")

                val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
                broadcastManager.unregisterReceiver(onBleButtonPressed)
                bleContext?.close()

                bleContext = BleContext(applicationContext, preferences.getString(MyPreference.device_address.name, ""))
                broadcastManager.registerReceiver(onBleButtonPressed, BleState.intentFilter(BleState.BUTTON_PRESSED))
                bleContext!!.connect()
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("BleService", "onUnbind()")
        return true
    }

    override fun onRebind(intent: Intent?) {
        Log.d("BleService", "onRebind()")
    }

    override fun onDestroy() {
        Log.d("BleService", "onDestroy()")

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.unregisterOnSharedPreferenceChangeListener(onPreferenceChanged)

        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.unregisterReceiver(onBleButtonPressed)

        bleContext?.close()
        bleContext = null
    }

}
