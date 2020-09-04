package com.ben.checkasenior.contacts

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.support.wear.widget.SwipeDismissFrameLayout
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.ben.checkasenior.AppPreferences
import com.ben.checkasenior.FirebaseUtilsWear
import com.ben.checkasenior.R
import com.ben.checkasenior.Utils
import com.ben.checkasenior.Utils.EXTRA_MESSAGE
import com.ben.checkasenior.Utils.EXTRA_NUMBER
import com.ben.checkasenior.Utils.SMS_DELIVERED_ACTION
import com.ben.checkasenior.Utils.SMS_SENT_ACTION
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable


class ContactHelpFragment : Fragment() {

    private var linear: LinearLayout? = null
    private  var contactingTextView:TextView? = null
    private  var helpTitle:TextView? = null


    private var resultsReceiver: BroadcastReceiver? = null
    private var intentFilter: IntentFilter? = null

    override fun onResume() {
        super.onResume()
        //this.activity.registerReceiver(resultsReceiver, intentFilter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_help, container, false)
        linear = view.findViewById(R.id.linear_container) as LinearLayout
        contactingTextView = view.findViewById(R.id.contacting) as TextView
        helpTitle = view.findViewById(R.id.helpTitle) as TextView

        view?.findViewById<SwipeDismissFrameLayout>(R.id.swipe_dismiss_root)?.apply {
            addCallback(object : SwipeDismissFrameLayout.Callback() {

                override fun onDismissed(layout: SwipeDismissFrameLayout) {
                    linear?.visibility = View.GONE
                    layout.visibility = View.GONE
                    fragmentManager.popBackStack()

                }
            })
        }

        resultsReceiver = SmsResultReceiver()
        intentFilter =  IntentFilter(SMS_SENT_ACTION)
        intentFilter?.addAction(SMS_DELIVERED_ACTION)

        contactEmergencies()



        return view
    }

    @SuppressLint("MissingPermission")
    private fun contactEmergencies() {
        if (!((this.activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).line1Number.isNullOrEmpty())) {
            val tMgr = this@ContactHelpFragment.activity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val mPhoneNumber = tMgr.line1Number

            var message = FirebaseUtilsWear.emergencySMSText.replace("PLACEHOLDER_PHONE", mPhoneNumber, false)
            if (AppPreferences.phoneNumber1.isNotEmpty()) {


                contactingTextView?.text = "Contacting ${AppPreferences.phoneNumber1}"
                sendSMS(AppPreferences.phoneNumber1, message)
            } else {
                contactingTextView?.text = "Please set up emergency contact"
            }

            if (AppPreferences.phoneNumber2.isNotEmpty()) {
                contactingTextView?.text = "Contacting ${AppPreferences.phoneNumber2}"
                sendSMS(AppPreferences.phoneNumber2, message)
            }

            if (AppPreferences.phoneNumber3.isNotEmpty()) {
                contactingTextView?.text = "Contacting ${AppPreferences.phoneNumber3}"
                sendSMS(AppPreferences.phoneNumber3, message)
            }

        } else {
            //TODO: Send SMS via phone
            contactingTextView?.text = "No service, sending sms via phone"
            val nodesTask: Task<List<Node>> = Wearable.getNodeClient(this@ContactHelpFragment.activity).connectedNodes
            nodesTask.addOnSuccessListener {
                it.forEach { node ->
                    val sendTask: Task<*> = Wearable.getMessageClient(this@ContactHelpFragment.activity).sendMessage(node.id, "/SEND_EMERGENCY_SMS", "SEND_EMERGENCY_SMS".toByteArray()).apply {
                        addOnSuccessListener {
                            contactingTextView?.text = "SMS via phone sent successfully"
                        }
                        addOnFailureListener {
                            contactingTextView?.text = "Unable to send sms via phone"
                        }
                    }

                }
            }
        }
    }

    private fun sendSMS(phoneNo: String, msg: String) {
        Utils.sendSMS(phoneNo, msg, this.activity)
        helpTitle?.text = "Emergency message sent"
        contactingTextView?.text = "A request for help has been sent to all emergency contacts on your list."

    }

    private inner class SmsResultReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // A simple result Toast text.
            var result: String? = null

            // Get the result action.
            val action = intent.action

            // Retrieve the recipient's number and message.
            val number = intent.getStringExtra(EXTRA_NUMBER)
            val message = intent.getStringExtra(EXTRA_MESSAGE)

            // This is the result for a send.
            if (SMS_SENT_ACTION.equals(action)) {
                val resultCode = resultCode
                result = "Send result : " + translateSentResult(resultCode)

            } else if (SMS_DELIVERED_ACTION.equals(action)) {
                var sms: SmsMessage? = null

                // A delivery result comes from the service
                // center as a simple SMS in a single PDU.
                val pdu = intent.getByteArrayExtra("pdu")
                val format = intent.getStringExtra("format")

                // Construct the SmsMessage from the PDU.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && format != null) {
                    sms = SmsMessage.createFromPdu(pdu, format)
                } else {
                    sms = SmsMessage.createFromPdu(pdu)
                }

                // getResultCode() is not reliable for delivery results.
                // We need to get the status from the SmsMessage.
                result = "Delivery result : " + translateDeliveryStatus(sms!!.getStatus())
            }// This is the result for a delivery.

            result = "$number, $message\n$result"
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
        }

        internal fun translateSentResult(resultCode: Int): String {
            when (resultCode) {
                Activity.RESULT_OK -> return "Activity.RESULT_OK"
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> return "SmsManager.RESULT_ERROR_GENERIC_FAILURE"
                SmsManager.RESULT_ERROR_RADIO_OFF -> return "SmsManager.RESULT_ERROR_RADIO_OFF"
                SmsManager.RESULT_ERROR_NULL_PDU -> return "SmsManager.RESULT_ERROR_NULL_PDU"
                SmsManager.RESULT_ERROR_NO_SERVICE -> return "SmsManager.RESULT_ERROR_NO_SERVICE"
                else -> return "Unknown error code"
            }
        }

        internal fun translateDeliveryStatus(status: Int): String {
            when (status) {
                Telephony.Sms.STATUS_COMPLETE -> return "Sms.STATUS_COMPLETE"
                Telephony.Sms.STATUS_FAILED -> return "Sms.STATUS_FAILED"
                Telephony.Sms.STATUS_PENDING -> return "Sms.STATUS_PENDING"
                Telephony.Sms.STATUS_NONE -> return "Sms.STATUS_NONE"
                else -> return "Unknown status code"
            }
        }
    }
}