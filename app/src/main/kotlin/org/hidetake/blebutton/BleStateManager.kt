package org.hidetake.blebutton

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Handler
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

class BleStateManager(context: Context, bleDeviceAddress: String) {

    private val bleDeviceAddress = bleDeviceAddress
    private val broadcastManager = LocalBroadcastManager.getInstance(context)
    private val broadcastReceivers = arrayListOf<BroadcastReceiver>()

    private var _currentState: BleState = BleState.INIT
    val currentState: BleState
        get() = _currentState

    fun receiveBleEvent(event: BleEvent) {
        val intent = BleEvent.intent(bleDeviceAddress, currentState, event)
        Log.d("BleStateManager", "Received event $event and broadcasting ${intent.action}")
        broadcastManager.sendBroadcast(intent)
    }

    fun addTransition(from: BleState, to: BleState, event: BleEvent) {
        val receiver = LambdaBroadcastReceiver { context, intent -> transit(to) }
        broadcastManager.registerReceiver(receiver, BleEvent.intentFilter(bleDeviceAddress, from, event))
        broadcastReceivers.add(receiver)
    }

    fun addTransition(from: BleState, to: BleState, delay: Long = 0) {
        val receiver = LambdaBroadcastReceiver { context, intent -> Handler().postDelayed({ transit(to) }, delay) }
        broadcastManager.registerReceiver(receiver, BleState.intentFilter(from))
        broadcastReceivers.add(receiver)
    }

    fun on(state: BleState, handler: () -> Unit) {
        val receiver = LambdaBroadcastReceiver { context, intent -> handler() }
        broadcastManager.registerReceiver(receiver, BleState.intentFilter(state))
        broadcastReceivers.add(receiver)
    }

    fun transitIf(current: BleState, to: BleState) {
        if (currentState == current) {
            transit(to)
        }
    }

    fun transit(to: BleState) {
        Log.d("BleStateManager", "Transit: $currentState -> $to")
        _currentState = to
        broadcastManager.sendBroadcast(BleState.intent(to))
    }

    fun close() {
        Log.d("BleStateManager", "Close: unregister ${broadcastReceivers.size} broadcast receivers")
        broadcastReceivers.forEach { receiver ->
            broadcastManager.unregisterReceiver(receiver)
        }
        transit(BleState.CLOSED)
    }

}
