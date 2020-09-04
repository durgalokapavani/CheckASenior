package com.ben.checkasenior

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

object Utils {

    const val KEY_HEART_RATE_RESULT = "KEY_HEART_RATE_RESULT"
    const val EXTRA_NUMBER = "number"
    const val EXTRA_MESSAGE = "message"
    const val SMS_SENT_ACTION = "com.mycompany.myapp.SMS_SENT"
    const val SMS_DELIVERED_ACTION = "com.mycompany.myapp.SMS_DELIVERED"

    const val FALL_DETECTED = "fall_detected"
    const val LOW_HR_DETECTED = "low_heart_rate_detected"
    const val HIGH_HR_DETECTED = "high_heart_rate_detected"
    const val SOS_TAPPED_WATCH = "sos_tapped_watch"
    const val SOS_CANCELLED_WATCH = "sos_cancelled_watch"


    fun sendSMS(phoneNo: String, msg: String, context: Context) {
        try {
            val sentIntent = Intent(SMS_SENT_ACTION)
            val deliveredIntent = Intent(SMS_DELIVERED_ACTION)

            // We attach the recipient's number and message to
            // the Intents for easy retrieval in the Receiver.
            sentIntent.putExtra(EXTRA_NUMBER, phoneNo)
            sentIntent.putExtra(EXTRA_MESSAGE, msg)
            deliveredIntent.putExtra(EXTRA_NUMBER, phoneNo)
            deliveredIntent.putExtra(EXTRA_MESSAGE, msg)
            val sentPI = PendingIntent.getBroadcast(context, 234, sentIntent, PendingIntent.FLAG_ONE_SHOT)

            val deliveredPI = PendingIntent.getBroadcast(context, 234, deliveredIntent, PendingIntent.FLAG_ONE_SHOT)

            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, msg, sentPI, deliveredPI)

        } catch (ex: Exception) {
            Toast.makeText(context, ex.message.toString(), Toast.LENGTH_SHORT).show()
            ex.printStackTrace()
        }

    }

    @SuppressLint("MissingPermission")
    fun contactEmergencies(context: Context) {
        if (!((context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).line1Number.isNullOrEmpty())) {
            val tMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val mPhoneNumber = tMgr.line1Number


            var message = FirebaseUtilsWear.emergencySMSText.replace("PLACEHOLDER_PHONE", mPhoneNumber, false)
            if (AppPreferences.phoneNumber1.isNotEmpty()) {

                sendSMS(AppPreferences.phoneNumber1, message, context)
            } else {
                Toast.makeText(context, "Please set up emergency contacts", Toast.LENGTH_SHORT).show()
            }

            if (AppPreferences.phoneNumber2.isNotEmpty()) {
                sendSMS(AppPreferences.phoneNumber2, message, context)
            }

            if (AppPreferences.phoneNumber3.isNotEmpty()) {
                sendSMS(AppPreferences.phoneNumber3, message, context)
            }

        } else {
            val nodesTask: Task<List<Node>> = Wearable.getNodeClient(context).connectedNodes
            nodesTask.addOnSuccessListener {
                it.forEach { node ->
                    val sendTask: Task<*> = Wearable.getMessageClient(context).sendMessage(node.id, "/SEND_EMERGENCY_SMS", "SEND_EMERGENCY_SMS".toByteArray()).apply {
                        addOnSuccessListener {
                            Toast.makeText(context, "SMS via phone sent successfully", Toast.LENGTH_SHORT).show()
                        }
                        addOnFailureListener {
                            Toast.makeText(context, "Unable to send sms via phone", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }
}

