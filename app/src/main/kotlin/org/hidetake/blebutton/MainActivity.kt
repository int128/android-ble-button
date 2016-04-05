package org.hidetake.blebutton

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d("ServiceConnection", "onServiceConnected()")
            if (binder is BleService.BleServiceBinder) {
                val bleContext = binder.bleContext

                val buttonAlert = findViewById(R.id.button_alert) as ImageButton
                buttonAlert.setOnClickListener { view ->
                    bleContext.immediateAlert()
                }

                val textLastTime = findViewById(R.id.text_last_time) as TextView
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                bleContext.onButtonPressed {
                    vibrator.vibrate(300)
                    textLastTime.text = SimpleDateFormat.getDateTimeInstance().format(Date())
                }

                val textStatus = findViewById(R.id.text_ble_status) as TextView
                bleContext.onConnecting {
                    buttonAlert.isEnabled = false
                    textStatus.text = "Connecting to BLE device..."
                }
                bleContext.onIdle {
                    buttonAlert.isEnabled = true
                    textStatus.text = "Connected to BLE device"
                }

                bleContext.connectOnFirstTime()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("ServiceConnection", "onServiceDisconnected()")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService(Intent(applicationContext, BleService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

}
