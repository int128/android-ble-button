package org.hidetake.blebutton

import android.bluetooth.*
import android.util.Log
import org.hidetake.blebutton.BleEvent.*

class BleCallback(bleStateManager: BleStateManager) : BluetoothGattCallback() {

    val bleStateManager = bleStateManager

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        Log.d("BLE", "onConnectionStateChange: newState=$newState")
        when (newState) {
            BluetoothProfile.STATE_CONNECTING -> bleStateManager.receiveBleEvent(BLE_CONNECTING)
            BluetoothProfile.STATE_CONNECTED -> bleStateManager.receiveBleEvent(BLE_CONNECTED)
            BluetoothProfile.STATE_DISCONNECTING -> bleStateManager.receiveBleEvent(BLE_DISCONNECTING)
            BluetoothProfile.STATE_DISCONNECTED -> bleStateManager.receiveBleEvent(BLE_DISCONNECTED)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        Log.d("BLE", "onServicesDiscovered: $status")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_SERVICE_DISCOVER_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_SERVICE_DISCOVER_FAILURE)
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Log.d("BLE", "onCharacteristicRead: $status: ${characteristic?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_FAILURE)
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Log.d("BLE", "onCharacteristicWrite: $status: ${characteristic?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_WRITE_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_WRITE_FAILURE)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        Log.d("BLE", "onCharacteristicChanged: ${characteristic?.uuid}")
        bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_CHANGED)
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        Log.d("BLE", "onDescriptorRead: $status: ${descriptor?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_FAILURE)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        Log.d("BLE", "onDescriptorWrite: $status: ${descriptor?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_WRITE_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_WRITE_SUCCESS)
        }
    }
}
