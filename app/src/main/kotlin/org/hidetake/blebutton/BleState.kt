package org.hidetake.blebutton

import android.content.Intent
import android.content.IntentFilter

enum class BleState {
    INIT,
    CONNECTING,
    CONNECTED,
    SERVICE_DISCOVERED,
    SENDING_LINK_LOSS,
    SENT_LINK_LOSS,
    SENDING_BUTTON_DESCRIPTOR,
    SENT_BUTTON_DESCRIPTOR,
    READY,
    IDLE,
    BUTTON_PRESSED,
    SENDING_ALARM_START,
    SENT_ALARM_START,
    SENDING_ALARM_STOP,
    SENT_ALARM_STOP,
    CLOSED,
    ;

    companion object {
        fun intent(bleState: BleState) = Intent("__$bleState")

        fun intentFilter(vararg bleStates: BleState): IntentFilter {
            val intentFilter = IntentFilter()
            bleStates.forEach { bleState -> intentFilter.addAction("__$bleState") }
            return intentFilter
        }
    }
}
