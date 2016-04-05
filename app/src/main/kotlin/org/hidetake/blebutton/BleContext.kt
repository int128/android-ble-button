package org.hidetake.blebutton

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import org.hidetake.blebutton.BleContext.BLE_UUID.*
import org.hidetake.blebutton.BleEvent.*
import org.hidetake.blebutton.BleState.*
import java.util.*

class BleContext(context: Context, address: String) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address.toUpperCase())
    private val bleStateManager = BleStateManager(context, bluetoothDevice.address)
    private val bluetoothGatt = bluetoothDevice.connectGatt(context, false, BleCallback(bleStateManager))

    val bleDeviceAddress: String
        get() = bluetoothDevice.address

    val bleState: BleState
        get() = bleStateManager.currentState

    enum class BLE_UUID(uuidString: String) {
        BUTTON_SERVICE("0000ffe0-0000-1000-8000-00805f9b34fb"),
        BUTTON_STATE("0000ffe1-0000-1000-8000-00805f9b34fb"),
        CLIENT_CHARACTERISTIC_CONFIG("00002902-0000-1000-8000-00805f9b34fb"),
        LINK_LOSS_SERVICE("00001803-0000-1000-8000-00805f9b34fb"),
        LINK_LOSS_ALERT_LEVEL("00002a06-0000-1000-8000-00805f9b34fb"),
        IMMEDIATE_ALERT_SERVICE("00001802-0000-1000-8000-00805f9b34fb"),
        IMMEDIATE_ALERT_LEVEL("00002a06-0000-1000-8000-00805f9b34fb"),
        ;
        val uuid = UUID.fromString(uuidString)
    }

    init {
        bleStateManager.on(CONNECTING) {
            bluetoothGatt.connect()
            Log.d("BleContext", "BluetoothGatt#connect() finished")
        }

        bleStateManager.addTransition(from = INIT, to = CONNECTED, event = BLE_CONNECTED)
        bleStateManager.addTransition(from = CONNECTING, to = CONNECTED, event = BLE_CONNECTED)

        bleStateManager.on(CONNECTED) {
            bluetoothGatt.discoverServices()
        }

        bleStateManager.addTransition(from = CONNECTED, to = SERVICE_DISCOVERED, event = BLE_SERVICE_DISCOVER_SUCCESS)
        bleStateManager.addTransition(from = CONNECTED, to = CONNECTED, event = BLE_SERVICE_DISCOVER_FAILURE)

        bleStateManager.addTransition(from = SERVICE_DISCOVERED, to = SENDING_BUTTON_DESCRIPTOR)

        bleStateManager.on(SENDING_BUTTON_DESCRIPTOR) {
            val characteristic = bluetoothGatt.getService(BUTTON_SERVICE.uuid).getCharacteristic(BUTTON_STATE.uuid)
            bluetoothGatt.setCharacteristicNotification(characteristic, true)

            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG.uuid)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt.writeDescriptor(descriptor)
        }

        bleStateManager.addTransition(from = SENDING_BUTTON_DESCRIPTOR, to = SENT_BUTTON_DESCRIPTOR, event = BLE_DESCRIPTOR_WRITE_SUCCESS)
        bleStateManager.addTransition(from = SENDING_BUTTON_DESCRIPTOR, to = SENDING_BUTTON_DESCRIPTOR, event = BLE_DESCRIPTOR_WRITE_FAILURE)

        bleStateManager.addTransition(from = SENT_BUTTON_DESCRIPTOR, to = SENDING_LINK_LOSS)

        bleStateManager.on(SENDING_LINK_LOSS) {
            val characteristic = bluetoothGatt.getService(LINK_LOSS_SERVICE.uuid).getCharacteristic(LINK_LOSS_ALERT_LEVEL.uuid)
            characteristic.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
            bluetoothGatt.writeCharacteristic(characteristic)
        }

        bleStateManager.addTransition(from = SENDING_LINK_LOSS, to = SENT_LINK_LOSS, event = BLE_CHARACTERISTIC_WRITE_SUCCESS)
        bleStateManager.addTransition(from = SENDING_LINK_LOSS, to = SENDING_LINK_LOSS, event = BLE_CHARACTERISTIC_WRITE_FAILURE)

        bleStateManager.addTransition(from = SENT_LINK_LOSS, to = READY)

        bleStateManager.addTransition(from = READY, to = IDLE)

        bleStateManager.addTransition(from = IDLE, to = BUTTON_PRESSED, event = BLE_CHARACTERISTIC_CHANGED)

        bleStateManager.addTransition(from = BUTTON_PRESSED, to = SENDING_ALARM_START, delay = 300)

        bleStateManager.on(SENDING_ALARM_START) {
            val characteristic = bluetoothGatt.getService(IMMEDIATE_ALERT_SERVICE.uuid).getCharacteristic(IMMEDIATE_ALERT_LEVEL.uuid)
            characteristic.setValue(2, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
            bluetoothGatt.writeCharacteristic(characteristic)
        }

        bleStateManager.addTransition(from = SENDING_ALARM_START, to = SENT_ALARM_START, event = BLE_CHARACTERISTIC_WRITE_SUCCESS)
        bleStateManager.addTransition(from = SENDING_ALARM_START, to = SENDING_ALARM_START, event = BLE_CHARACTERISTIC_WRITE_FAILURE)

        bleStateManager.addTransition(from = SENT_ALARM_START, to = SENDING_ALARM_STOP, delay = 300)

        bleStateManager.on(SENDING_ALARM_STOP) {
            val characteristic = bluetoothGatt.getService(IMMEDIATE_ALERT_SERVICE.uuid).getCharacteristic(IMMEDIATE_ALERT_LEVEL.uuid)
            characteristic.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
            bluetoothGatt.writeCharacteristic(characteristic)
        }

        bleStateManager.addTransition(from = SENDING_ALARM_STOP, to = SENT_ALARM_STOP, event = BLE_CHARACTERISTIC_WRITE_SUCCESS)
        bleStateManager.addTransition(from = SENDING_ALARM_STOP, to = SENDING_ALARM_STOP, event = BLE_CHARACTERISTIC_WRITE_FAILURE)

        bleStateManager.addTransition(from = SENT_ALARM_STOP, to = IDLE)

        BleState.values().subtract(setOf(CLOSED)).forEach { notClosedState ->
            bleStateManager.addTransition(from = notClosedState, to = CONNECTING, event = BLE_DISCONNECTED)
        }
    }

    fun connect() {
        Log.d("BleContext", "connect()")
        bleStateManager.transitIf(current = INIT, to = CONNECTING)
    }

    fun immediateAlert() {
        Log.d("BleContext", "immediateAlert()")
        bleStateManager.transitIf(current = IDLE, to = SENDING_ALARM_START)
    }

    fun close() {
        Log.d("BleContext", "close()")
        bleStateManager.close()
        bluetoothGatt.disconnect()
        bluetoothGatt.close()
    }

}