package com.ben.checkasenior

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.text.TextUtils
import android.widget.Toast

object Constants {

    const val EMERGENCY_CONTACT1_UPDATED = "emergency_contact1_updated"
    const val EMERGENCY_CONTACT2_UPDATED = "emergency_contact2_updated"
    const val EMERGENCY_CONTACT3_UPDATED = "emergency_contact3_updated"
    const val EMERGENCY_SMS_SENT = "emergency_sms_sent"
    const val EMERGENCY_SMS_SENT_VIA_PHONE = "emergency_sms_sent_via_phone"
    const val TOTAL_CONTACTS = "total_contacts"
    const val SOS_TAPPED = "sos_tapped"
    const val SOS_CANCELLED = "sos_cancelled"
    const val PHONE_TAPPED = "phone_tapped"
    const val SMS_TAPPED = "sms_tapped"
    const val SUBSCRIPTION_BUTTON_TAPPED = "subscription_button_tapped"

    val EXTRA_NUMBER = "number"
    val EXTRA_MESSAGE = "message"
    val SMS_SENT_ACTION = "com.mycompany.myapp.SMS_SENT"
    val SMS_DELIVERED_ACTION = "com.mycompany.myapp.SMS_DELIVERED"

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

    fun isValidPhone(phone: CharSequence): Boolean {
        return if (TextUtils.isEmpty(phone) || (phone.count() < 9 || phone.count() > 13)) {
            false
        } else {
            println("phone: $phone")
            println("VALID: "+ android.util.Patterns.PHONE.matcher(phone).matches())
            android.util.Patterns.PHONE.matcher(phone).matches()
        }
    }

}

