package org.hidetake.blebutton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LambdaBroadcastReceiver(receiver: (context: Context?, intent: Intent?) -> Unit) : BroadcastReceiver() {

    private val receiver = receiver

    override fun onReceive(context: Context?, intent: Intent?) = receiver(context, intent)

}
