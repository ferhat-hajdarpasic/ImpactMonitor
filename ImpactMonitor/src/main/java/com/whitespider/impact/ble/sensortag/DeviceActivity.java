package com.whitespider.impact.ble.sensortag;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;


import com.whitespider.impact.ble.btsig.profiles.DeviceInformationServiceProfile;
import com.whitespider.impact.ble.common.BluetoothLeService;
import com.whitespider.impact.ble.common.GattInfo;
import com.whitespider.impact.ble.common.GenericBluetoothProfile;
import com.whitespider.impact.ble.ti.profiles.TIOADProfile;
import com.whitespider.impact.history.HistoryItem;


@SuppressLint("InflateParams")
public class DeviceActivity extends ViewPagerActivity {
	// Log
	// private static String TAG = "DeviceActivity";

	// Activity
	public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
	private static final int PREF_ACT_REQ = 0;
	static final int FWUPDATE_ACT_REQ = 1;

	DeviceView mDeviceView = null;

	// BLE
	BluetoothLeService mBtLeService = null;
	BluetoothDevice mBluetoothDevice = null;
	BluetoothGatt mBtGatt = null;
	boolean mServicesRdy = false;
	private boolean mIsReceiving = false;

	// SensorTagGatt
	BluetoothGattService mOadService = null;
	BluetoothGattService mConnControlService = null;
	DeviceActivityBroadcastReceiver mGattUpdateReceiver = null;
	private boolean mIsSensorTag2;
	public ProgressDialog progressDialog;

	//GUI
	List<GenericBluetoothProfile> mProfiles;
	private SensorTagIoProfile sensorTagIoProfile;
	private HistoryItem feedItem;

	public DeviceActivity() {
		mResourceFragmentPager = R.layout.fragment_pager;
		mResourceIdPager = R.id.pager;
	}

	public static DeviceActivity getInstance() {
		return (DeviceActivity) mThis;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		// BLE
		mBtLeService = BluetoothLeService.getInstance();
		mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);

		mIsSensorTag2 = false;
		// Determine type of SensorTagGatt
		String deviceName = mBluetoothDevice.getName();
		if ((deviceName.equals("SensorTag2")) ||(deviceName.equals("CC2650 SensorTag"))) {
			mIsSensorTag2 = true;
		} else {
			mIsSensorTag2 = false;
		}

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// GUI
		mDeviceView = new DeviceView();
		mSectionsPagerAdapter.addSection(mDeviceView, "ID= " + mBluetoothDevice.getAddress());
		//HelpView hw = new HelpView();
		//hw.setParameters("help_device.html", R.layout.fragment_help, R.id.webpage);
		//mSectionsPagerAdapter.addSection(hw, "Help");
		mProfiles = new ArrayList<GenericBluetoothProfile>();
		progressDialog = createProgressDialog("Discovering Services", "");
        progressDialog.show();

        // GATT database
		Resources res = getResources();
		XmlResourceParser xpp = res.getXml(R.xml.gatt_uuid);
		new GattInfo(xpp);
	}

	public ProgressDialog createProgressDialog(String title, String message) {
		ProgressDialog result = new ProgressDialog(DeviceActivity.this);
		result.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		result.setIndeterminate(true);
		result.setTitle(title);
		result.setMessage(message);
		result.setMax(100);
		result.setProgress(0);
		return result;
	}

	public static float getNumber(Activity activity, SharedPreferences prefs,
								int sampling_frequency, float defaultValue) {
		String key = activity.getResources().getString(sampling_frequency);
		return Float.parseFloat(prefs.getString(key, "" + defaultValue));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        if (!this.isEnabledByPrefs("keepAlive")) {
            this.mBtLeService.timedDisconnect();
        }
        //View should be started again from scratch
        this.mDeviceView.first = true;
        this.mProfiles = null;
        this.mDeviceView.removeRowsFromTable();
        this.mDeviceView = null;
		finishActivity(PREF_ACT_REQ);
		finishActivity(FWUPDATE_ACT_REQ);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.device_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.opt_prefs:
			startPreferenceActivity(mBluetoothDevice, this);
			break;
		case R.id.visual:
			Intent intent = new Intent(this, VisualHeadActivity.class);
			float x = 4.4f;
			float y = 4.4f;
			float z = 4.5f;
			if(feedItem != null) {
				x = (float)feedItem.getDirection().x;
				y = (float)feedItem.getDirection().y;
				z = (float)feedItem.getDirection().z;
			}
			intent.putExtra("accelerationX", x);
			intent.putExtra("accelerationY", y);
			intent.putExtra("accelerationZ", z);
			startActivityForResult(intent, 1);

			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	public boolean isEnabledByPrefs(String prefName) {
		String preferenceKeyString = "pref_"
				+ prefName;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBtLeService);

		Boolean defaultValue = true;
		return prefs.getBoolean(preferenceKeyString, defaultValue);
	}
	@Override
	protected void onResume() {
		super.onResume();
		mBtGatt = BluetoothLeService.getBtGatt();
		if(mGattUpdateReceiver != null) {
			unregisterReceiver(mGattUpdateReceiver);
			mGattUpdateReceiver = null;
		}
		if(mGattUpdateReceiver == null) {
			mGattUpdateReceiver = new DeviceActivityBroadcastReceiver(this);
			registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		}
		mBtGatt.discoverServices();
//		this.mBtLeService.abortTimedDisconnect();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	public static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter fi = new IntentFilter();
		fi.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		fi.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
		fi.addAction(BluetoothLeService.ACTION_DATA_WRITE);
		fi.addAction(BluetoothLeService.ACTION_DATA_READ);
		fi.addAction(DeviceInformationServiceProfile.ACTION_FW_REV_UPDATED);
        fi.addAction(TIOADProfile.ACTION_PREPARE_FOR_OAD);
		return fi;
	}

	boolean isSensorTag2() {
		return mIsSensorTag2;
	}

	BluetoothGattService getOadService() {
		return mOadService;
	}

	BluetoothGattService getConnControlService() {
		return mConnControlService;
	}

	public static void startPreferenceActivity(BluetoothDevice bluetoothDevice, Activity activity) {
		// Launch preferences
		final Intent i = new Intent(activity, PreferencesActivity.class);
		i.putExtra(PreferencesActivity.EXTRA_SHOW_FRAGMENT,
				PreferencesFragment.class.getName());
		i.putExtra(PreferencesActivity.EXTRA_NO_HEADERS, true);
		i.putExtra(EXTRA_DEVICE, bluetoothDevice);
		activity.startActivityForResult(i, PREF_ACT_REQ);
	}

	protected void setBusy(boolean b) {
		mDeviceView.setBusy(b);
	}
	// Activity result handling
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		default:
			break;
		}
	}

	void setError(String txt) {
		setBusy(false);
		Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
	}

	private void setStatus(String txt) {
		Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
	}
	protected void observeAcceleration(MotionSensor p) {
		final Motion reading = p.getReading();

		final double totalAcceleration = ConcussionDetector.getTotalAcceleration(reading);
		Log.d("#", "Total acceleration=" + totalAcceleration);
		if (totalAcceleration >= 2.0) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					try {
						Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						Ringtone r = RingtoneManager.getRingtone(DeviceActivity.this, notification);
						r.play();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void enableService(final GenericBluetoothProfile p) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				p.enableService();
				progressDialog.setProgress(progressDialog.getProgress() + 1);
			}
		});
	}


	public void setSensorTagIoProfile(SensorTagIoProfile sensorTagIoProfile) {
		this.sensorTagIoProfile = sensorTagIoProfile;
	}

	public SensorTagIoProfile getSensorTagIoProfile() {
		return this.sensorTagIoProfile;
	}

	public void showHead(View view) {
		feedItem = (HistoryItem)view.getTag();
		Intent intent = new Intent(this, VisualHeadActivity.class);
		float x = 4.4f;
		float y = 4.4f;
		float z = 4.5f;
		if(feedItem != null) {
			x = (float)feedItem.getDirection().x;
			y = (float)feedItem.getDirection().y;
			z = (float)feedItem.getDirection().z;
		}
		intent.putExtra("accelerationX", x);
		intent.putExtra("accelerationY", y);
		intent.putExtra("accelerationZ", z);
		startActivityForResult(intent, 1);
	}
}
