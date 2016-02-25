package com.whitespider.impact.ble.sensortag;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.whitespider.impact.ble.common.BluetoothLeService;
import com.whitespider.impact.ble.common.HCIDefines;

/**
 * Created by ferhat on 2/24/2016.
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothBcastReceiver";
    private final MainActivity mainActivity;

    public BluetoothBroadcastReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            // Bluetooth adapter state change
            switch (mainActivity.mBtAdapter.getState()) {
                case BluetoothAdapter.STATE_ON:
                    mainActivity.mConnIndex = MainActivity.NO_DEVICE;
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(context, R.string.app_closing, Toast.LENGTH_LONG)
                            .show();
                    mainActivity.finish();
                    break;
                default:
                    // Log.w(TAG, "Action STATE CHANGED not processed ");
                    break;
            }

            mainActivity.updateGuiState();
        } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            // GATT connect
            int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mainActivity.setBusy(false, "");
                //startDeviceActivity();
                mainActivity.startHeadGearActivity();
            } else {
                mainActivity.setError("Connect failed. Status: " + status);
            }
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            // GATT disconnect
            int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
            mainActivity.stopDeviceActivity();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mainActivity.setBusy(false, "");
                mainActivity.mScanView.setStatus(mainActivity.mBluetoothDevice.getName() + " disconnected", MainActivity.STATUS_DURATION);
            } else {
                mainActivity.setError("Disconnect Status: " + HCIDefines.hciErrorCodeStrings.get(status));
            }
            mainActivity.mConnIndex = MainActivity.NO_DEVICE;
            mainActivity.mBluetoothLeService.close();
        } else {
            Log.w(TAG, "Unknown action: " + action);
        }

    }
}
