package org.hidetake.blebutton

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem

class DeviceActivity : AppCompatActivity() {

    private var paused = false

    val serviceConnection = BleServiceConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DeviceActivity", "onCreate()")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.device_activity_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.device_activity_menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        Log.d("DeviceActivity", "onStart()")
        super.onStart()

        val serviceIntent = Intent(applicationContext, BleService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.registerReceiver(onConnectionStateChanged, BleState.intentFilter(BleState.CONNECTING, BleState.IDLE))
    }

    private val onConnectionStateChanged = LambdaBroadcastReceiver { context, intent ->
        updateFragmentState()
    }

    override fun onPause() {
        Log.d("DeviceActivity", "onPause()")
        super.onPause()
        paused = true
    }

    override fun onResume() {
        Log.d("DeviceActivity", "onResume()")
        super.onResume()
        paused = false

        updateFragmentState()
    }

    override fun onStop() {
        Log.d("DeviceActivity", "onStop()")

        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.unregisterReceiver(onConnectionStateChanged)

        if (isFinishing) {
            val serviceIntent = Intent(applicationContext, BleService::class.java)
            unbindService(serviceConnection)
            stopService(serviceIntent)
        }

        super.onStop()
    }

    private fun updateFragmentState() {
        if (!paused) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.layout_container, when (serviceConnection.service?.bleState) {
                        BleState.IDLE -> ConnectedFragment()
                        else -> ConnectingFragment()
                    })
                    .commit()
        }
    }

}
