package com.whitespider.impact.ble.sensortag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
// import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.whitespider.impact.ble.common.BleDeviceInfo;
import com.whitespider.impact.ble.common.BluetoothLeService;
import com.whitespider.impact.ble.common.HCIDefines;
import com.whitespider.impact.util.CustomToast;

public class MainActivity extends ViewPagerActivity {
	private static final String TAG = "MainActivity";

	// Requests to other activities
	private static final int REQ_ENABLE_BT = 0;
	private static final int REQ_DEVICE_ACT = 1;

	// GUI
	private static MainActivity mThis = null;
	ScanView mScanView;
	private Intent mDeviceIntent;
	static final int STATUS_DURATION = 5;

	// BLE management
	private boolean mBleSupported = true;
	private boolean mScanning = false;
	private int mNumDevs = 0;
	int mConnIndex = NO_DEVICE;
	private List<BleDeviceInfo> mDeviceInfoList;
	private static BluetoothManager mBluetoothManager;
	BluetoothAdapter mBtAdapter = null;
	BluetoothDevice mBluetoothDevice = null;
	BluetoothLeService mBluetoothLeService = null;
	private IntentFilter mFilter;
	private String[] mDeviceFilter = null;

	// Housekeeping
	static final int NO_DEVICE = -1;
	private boolean mInitialised = false;
	SharedPreferences prefs = null;
	private BroadcastReceiver mReceiver;

	public MainActivity() {
		mThis = this;
		mResourceFragmentPager = R.layout.fragment_pager;
		mResourceIdPager = R.id.pager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Start the application
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Initialize device list container and device filter
		mDeviceInfoList = new ArrayList<BleDeviceInfo>();
		Resources res = getResources();
		mDeviceFilter = res.getStringArray(R.array.device_filter);

		// Create the fragments and add them to the view pager and tabs
		mScanView = new ScanView();
		mSectionsPagerAdapter.addSection(mScanView, "Sensors");

		// Register the BroadcastReceiver
		mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
    }



	@Override
	public void onDestroy() {
		// Log.e(TAG,"onDestroy");
		super.onDestroy();
		mBtAdapter = null;
		// Clear cache
		File cache = getCacheDir();
		String path = cache.getPath();
    try {
	    Runtime.getRuntime().exec(String.format("rm -rf %s", path));
    } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.opt_prefs:
			DeviceActivity.startPreferenceActivity(mBluetoothDevice, this);
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	void onScanViewReady(View view) {
		// License popup on first run
		if (prefs.getBoolean("firstrun", true)) {
			prefs.edit().putBoolean("firstrun", false).commit();
		}

		if (!mInitialised) {
			// Broadcast receiver
            mBluetoothLeService = BluetoothLeService.getInstance();
            mBluetoothManager = mBluetoothLeService.getBtManager();
            mBtAdapter = mBluetoothManager.getAdapter();
			if(mReceiver == null) {
				mReceiver = new BluetoothBroadcastReceiver(this);
				registerReceiver(mReceiver, mFilter);
			}
			if (!mBtAdapter.isEnabled()) {
				// Request BT adapter to be turned on
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQ_ENABLE_BT);
			}
			mInitialised = true;
		} else {
			mScanView.notifyDataSetChanged();
		}
        // Initial state of widgets
        updateGuiState();
	}

	public void onBtnScan(View view) {
		if (mScanning) {
			stopScan();
		} else {
			startScan();
		}
	}

	void onConnect() {
		if (mNumDevs > 0) {
			int connState = mBluetoothManager.getConnectionState(mBluetoothDevice,BluetoothGatt.GATT);
			switch (connState) {
			case BluetoothGatt.STATE_CONNECTED:
				mBluetoothLeService.disconnect(null);
				break;
			case BluetoothGatt.STATE_DISCONNECTED:
				boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
				if (!ok) {
					setError("Connect failed");
				}
				break;
			default:
				setError("Device busy (connecting/disconnecting)");
				break;
			}
		}
	}

	private void startScan() {
		// Start device discovery
		if (mBleSupported) {
			mNumDevs = 0;
			mDeviceInfoList.clear();
			mScanView.notifyDataSetChanged();
			scanLeDevice(true);
			mScanView.updateGui(mScanning);
			if (!mScanning) {
				setError("Device discovery start failed");
				setBusy(false, "");
			}
		} else {
			setError("BLE not supported on this device");
		}

	}

	private void stopScan() {
		mScanning = false;
		mScanView.updateGui(false);
		scanLeDevice(false);
	}

	private void startDeviceActivity() {
		mDeviceIntent = new Intent(this, DeviceActivity.class);
		mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
		startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
	}

	void startHeadGearActivity() {
		mDeviceIntent = new Intent(this, HeadGearActivity.class);
		mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
		startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
	}

	void stopDeviceActivity() {
		finishActivity(REQ_DEVICE_ACT);
	}

	public void onDeviceClick(final int pos) {

		if (mScanning)
			stopScan();

		setBusy(true, "Connecting...");
		mBluetoothDevice = mDeviceInfoList.get(pos).getBluetoothDevice();
		if (mConnIndex == NO_DEVICE) {
			mScanView.setStatus("Connecting");
			mConnIndex = pos;
			onConnect();
		} else {
			mScanView.setStatus("Disconnecting");
			if (mConnIndex != NO_DEVICE) {
				mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			}
		}
	}

	public void onScanTimeout() {
		runOnUiThread(new Runnable() {
			public void run() {
				stopScan();
			}
		});
	}

	public void onConnectTimeout() {
		runOnUiThread(new Runnable() {
			public void run() {
				setError("Connection timed out");
			}
		});
		if (mConnIndex != NO_DEVICE) {
			mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			mConnIndex = NO_DEVICE;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// GUI methods
	//
	public void updateGuiState() {
		boolean mBtEnabled = mBtAdapter.isEnabled();

		if (mBtEnabled) {
			if (mScanning) {
				// BLE Host connected
				if (mConnIndex != NO_DEVICE) {
					String txt = mBluetoothDevice.getName() + " connected";
					mScanView.setStatus(txt);
				} else {
					mScanView.setStatus(mNumDevs + " devices");
				}
			}
		} else {
			mDeviceInfoList.clear();
			mScanView.notifyDataSetChanged();
		}
	}

	void setBusy(boolean f, String message) {
		mScanView.setBusy(f, message);
	}

	void setError(String txt) {
		mScanView.setError(txt);
		//CustomToast.middleBottom(this, "Turning BT adapter off and on again may fix Android BLE stack problems");
	}

	private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi) {
		BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi);

		return deviceInfo;
	}

	boolean checkDeviceFilter(String deviceName) {
		if (deviceName == null)
			return false;

		int n = mDeviceFilter.length;
		if (n > 0) {
			boolean found = false;
			for (int i = 0; i < n && !found; i++) {
				found = deviceName.equals(mDeviceFilter[i]);
			}
			return found;
		} else
			// Allow all devices if the device filter is empty
			return true;
	}

	private void addDevice(BleDeviceInfo device) {
		mNumDevs++;
		mDeviceInfoList.add(device);
		mScanView.notifyDataSetChanged();
		if (mNumDevs > 1)
			mScanView.setStatus(mNumDevs + " devices");
		else
			mScanView.setStatus("1 device");
	}

	private boolean deviceInfoExists(String address) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
			    .equals(address)) {
				return true;
			}
		}
		return false;
	}

	private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
			    .equals(device.getAddress())) {
				return mDeviceInfoList.get(i);
			}
		}
		return null;
	}

	private boolean scanLeDevice(boolean enable) {
		if (enable) {
			mScanning = mBtAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBtAdapter.stopLeScan(mLeScanCallback);
		}
		return mScanning;
	}

	List<BleDeviceInfo> getDeviceInfoList() {
		return mDeviceInfoList;
	}



	// Activity result handling
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQ_DEVICE_ACT:
			// When the device activity has finished: disconnect the device
			if (mConnIndex != NO_DEVICE) {
				mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			}
			break;

		case REQ_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {

				Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
			} else {
				// User did not enable Bluetooth or an error occurred
				Toast.makeText(this, R.string.bt_not_on, Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		default:
			CustomToast.middleBottom(this, "Unknown request code: " + requestCode);

			// Log.e(TAG, "Unknown request code");
			break;
		}
	}

	// Device scan callback.
	// NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		public void onLeScan(final BluetoothDevice device, final int rssi,
		    byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				public void run() {
					// Filter devices
					if (checkDeviceFilter(device.getName())) {
						if (!deviceInfoExists(device.getAddress())) {
							// New device
							BleDeviceInfo deviceInfo = createDeviceInfo(device, rssi);
							addDevice(deviceInfo);
						} else {
							// Already in list, update RSSI info
							BleDeviceInfo deviceInfo = findDeviceInfo(device);
							deviceInfo.updateRssi(rssi);
							mScanView.notifyDataSetChanged();
						}
					}
				}

			});
		}
	};
}
