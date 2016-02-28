package com.whitespider.impact.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.whitespider.impact.ble.sensortag.R;

/**
 * Created by ferhat on 2/28/2016.
 */
public class BrainGearSmsSender {

    private final String deviceName;
    Activity mActivity;
    public static final String SMS_SENT = "SMS_SENT";
    public static final String SMS_DELIVERED = "SMS_DELIVERED";
    private final PendingIntent sentPI;
    private final PendingIntent deliveredPI;
    private final BroadcastReceiver smsDeliveredBroadcastReceiver;
    private final BroadcastReceiver smsSentBroadcastReceiver;
    private final String smsAlertPhoneNumber;
    private boolean mIsSendingSms;

    public BrainGearSmsSender(Activity activity, SharedPreferences prefs) {
        String phoneNumberKey = activity.getResources().getString(R.string.smsAlertPhoneNumber);
        smsAlertPhoneNumber = prefs.getString(phoneNumberKey, "");
        String deviceNameKey = activity.getResources().getString(R.string.headgear_device_name);
        deviceName = prefs.getString(deviceNameKey, "Edwin");

        this.mActivity = activity;
        sentPI = PendingIntent.getBroadcast(mActivity, 0, new Intent(SMS_SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(mActivity, 0, new Intent(SMS_DELIVERED), 0);

        smsSentBroadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(mActivity, "SMS sent",Toast.LENGTH_SHORT).show();
                        mIsSendingSms = false;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(mActivity, "Generic failure",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(mActivity, "No service",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(mActivity, "Null PDU",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(mActivity, "Radio off",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        mActivity.registerReceiver(smsSentBroadcastReceiver, new IntentFilter(SMS_SENT));

        smsDeliveredBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(mActivity, "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(mActivity, "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        mActivity.registerReceiver(smsDeliveredBroadcastReceiver, new IntentFilter(SMS_DELIVERED));
    }

    public void sendSms(String concussionEventSeverity, String concussionEventTime) {
        if((smsAlertPhoneNumber!= null) && (smsAlertPhoneNumber.trim().length() > 0)) {
            if(!mIsSendingSms) {
                String message = "Concussion event [" + concussionEventSeverity + "] for " + deviceName + " on " + concussionEventTime;
                SmsManager.getDefault().sendTextMessage(smsAlertPhoneNumber, null, message, sentPI, deliveredPI);
                mIsSendingSms = true;
            }
        }
    }

    public void onDestroy() {
        mActivity.unregisterReceiver(smsDeliveredBroadcastReceiver);
        mActivity.unregisterReceiver(smsSentBroadcastReceiver);
    }
}
