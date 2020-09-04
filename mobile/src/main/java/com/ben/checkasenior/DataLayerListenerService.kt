package com.ben.checkasenior

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*

private const val TAG = "DataLayerService"

class DataLayerListenerService : WearableListenerService() {


    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(
                TAG,
                "onMessageReceived() A message from watch was received:"
                        + messageEvent.requestId
                        + " "
                        + messageEvent.path)
        if (messageEvent.path == "/WATCH_APP_OPENED") {
            sendAllEmergencyContacts()
        }
        if (messageEvent.path == "/SEND_EMERGENCY_SMS") {

            sendSMSToAllEmergencyContacts()
        }

    }

    public fun sendAllEmergencyContacts() {
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/emergencyContacts").run {
            dataMap.putString(UserAppPreferences.EMERGENCY_PHONE1.first, UserAppPreferences.phoneNumber1)
            dataMap.putString(UserAppPreferences.EMERGENCY_PHONE2.first, UserAppPreferences.phoneNumber2)
            dataMap.putString(UserAppPreferences.EMERGENCY_PHONE3.first, UserAppPreferences.phoneNumber3)

            dataMap.putString(UserAppPreferences.EMERGENCY_PHONE1_NAME.first, UserAppPreferences.phoneNumber1Name)
            dataMap.putString(UserAppPreferences.EMERGENCY_PHONE2_NAME.first, UserAppPreferences.phoneNumber2Name)
            dataMap.putString(UserAppPreferences.EMERGENCY_PHONE3_NAME.first, UserAppPreferences.phoneNumber3Name)
            asPutDataRequest()
        }
        putDataReq.setUrgent()
        val putDataTask: Task<DataItem> = Wearable.getDataClient(this).putDataItem(putDataReq)
        putDataTask.addOnSuccessListener {
            Log.d(TAG, "Sending ALL contacts was successful: $it")
        }

        putDataTask.addOnFailureListener {
            Log.d(TAG, "Sending ALL contacts Failed: $it")
        }


    }

    @SuppressLint("MissingPermission")
    private fun sendSMSToAllEmergencyContacts(){

        var message = FirebaseUtils.emergencySMSText
        val tMgr = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val mPhoneNumber = tMgr.line1Number

        message = message.replace("PLACEHOLDER_PHONE" , mPhoneNumber, false)

        if(UserAppPreferences.phoneNumber1.isNotEmpty() && Constants.isValidPhone(UserAppPreferences.phoneNumber1)) {
            Constants.sendSMS(UserAppPreferences.phoneNumber1, message, this)
        }
        if(UserAppPreferences.phoneNumber2.isNotEmpty() && Constants.isValidPhone(UserAppPreferences.phoneNumber2)) {
            Constants.sendSMS(UserAppPreferences.phoneNumber2, message, this)
        }
        if(UserAppPreferences.phoneNumber3.isNotEmpty() && Constants.isValidPhone(UserAppPreferences.phoneNumber3)) {
            Constants.sendSMS(UserAppPreferences.phoneNumber3, message, this)
        }

        CheckASeniorApp.instance.firebaseAnalytics.logEvent(Constants.EMERGENCY_SMS_SENT_VIA_PHONE, null)

    }
}