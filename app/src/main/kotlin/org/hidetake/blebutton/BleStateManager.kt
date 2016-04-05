package org.hidetake.blebutton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

class BleStateManager(context: Context, address: String) {

    private val broadcastManager = LocalBroadcastManager.getInstance(context)

    private var currentState: BleState = BleState.INIT

    private val deviceId = address.replace(":", "")

    private fun getNameOfIntentOnTransition(state: BleState): String {
        return "${deviceId}__$state"
    }

    private fun getNameOfIntentOnEvent(state: BleState, event: BleEvent): String {
        return "${deviceId}__${state}__$event"
    }

    fun receiveBleEvent(event: BleEvent) {
        val intentName = getNameOfIntentOnEvent(currentState, event)
        Log.d("BLE", "Received event $event and broadcasting $intentName")
        broadcastManager.sendBroadcast(Intent(intentName))
    }

    fun addTransition(from: BleState, to: BleState, event: BleEvent) {
        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                transit(to)
            }
        }, IntentFilter(getNameOfIntentOnEvent(from, event)))
    }

    fun addTransition(from: BleState, to: BleState, delay: Long = 0) {
        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Handler().postDelayed({ transit(to) }, delay)
            }
        }, IntentFilter(getNameOfIntentOnTransition(from)))
    }

    fun on(state: BleState, handler: () -> Unit) {
        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                handler()
            }
        }, IntentFilter(getNameOfIntentOnTransition(state)))
    }

    fun transitIf(current: BleState, to: BleState) {
        if (currentState == current) {
            transit(to)
        }
    }

    fun transit(to: BleState) {
        Log.d("BLE", "Transit: $currentState -> $to")
        currentState = to
        broadcastManager.sendBroadcast(Intent(getNameOfIntentOnTransition(to)))
    }

}
