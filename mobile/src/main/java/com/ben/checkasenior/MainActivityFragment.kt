package com.ben.checkasenior

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ben.checkasenior.Constants.EMERGENCY_CONTACT1_UPDATED
import com.ben.checkasenior.Constants.EMERGENCY_CONTACT2_UPDATED
import com.ben.checkasenior.Constants.EMERGENCY_CONTACT3_UPDATED
import com.ben.checkasenior.Constants.EMERGENCY_SMS_SENT
import com.ben.checkasenior.Constants.PHONE_TAPPED
import com.ben.checkasenior.Constants.SMS_TAPPED
import com.ben.checkasenior.Constants.SOS_CANCELLED
import com.ben.checkasenior.Constants.SOS_TAPPED
import com.ben.checkasenior.Constants.SUBSCRIPTION_BUTTON_TAPPED
import com.ben.checkasenior.Constants.TOTAL_CONTACTS
import com.ben.checkasenior.Constants.isValidPhone
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.util.*
import kotlin.concurrent.schedule

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    private var sosView: CircularProgressBar? = null
    private var tapToCancel: TextView? = null
    private var subscriptionButton: Button? = null
    private var name1: EditText? = null
    private var phoneNumber1: EditText? = null
    private var name2: EditText? = null
    private var phoneNumber2: EditText? = null
    private var name3: EditText? = null
    private var phoneNumber3: EditText? = null
    private var callPhone1: Button? = null
    private var smsPhone1: Button? = null
    private var callPhone2: Button? = null
    private var smsPhone2: Button? = null
    private var callPhone3: Button? = null
    private var smsPhone3: Button? = null

    private var countDownTimer:CountDownTimer? = null

    private var timerRunning:Boolean = false
    var toneG: ToneGenerator? = null
    var packageManager: PackageManager? = null
    var audioManager: AudioManager? = null
    var phoneNumberToCall = ""
    var phoneNumberToSMS = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)


        packageManager = this.activity?.packageManager
        audioManager = this.activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        toneG = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100)

        tapToCancel = view.findViewById(R.id.tapToCancel) as TextView
        subscriptionButton = view.findViewById(R.id.subscriptionButton) as Button
        subscriptionButton?.setOnClickListener {
            CheckASeniorApp.instance.firebaseAnalytics.logEvent(SUBSCRIPTION_BUTTON_TAPPED, null)
            openURL(FirebaseUtils.subscriptionURL, this@MainActivityFragment.activity!!)

        }
        sosView = view.findViewById(R.id.circularProgressBar) as CircularProgressBar
        sosView?.setOnClickListener {

            CheckASeniorApp.instance.firebaseAnalytics.logEvent(SOS_TAPPED, null)

            if(timerRunning) {
                this.countDownTimer?.cancel()
                CheckASeniorApp.instance.firebaseAnalytics.logEvent(SOS_CANCELLED, null)

                toneG?.stopTone()
                sosView?.setProgressWithAnimation(0F)
                tapToCancel?.visibility = View.INVISIBLE
                this.timerRunning = false
            } else {
                this.countDownTimer = object:CountDownTimer(5000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        tapToCancel?.visibility = View.VISIBLE
                        beep(300)

                    }

                    override fun onFinish() {
                        this@MainActivityFragment.timerRunning = false
                        sosView?.setProgressWithAnimation(0F, 1000)
                        tapToCancel?.visibility = View.INVISIBLE

                        sendSMSToAllEmergencyContacts()

                    }
                }

                sosView?.setProgressWithAnimation(100F, 5000)
                this.countDownTimer?.start()
                this.timerRunning = true
            }

        }

        name1 = view.findViewById(R.id.name1) as EditText
        name1?.setText(UserAppPreferences.phoneNumber1Name)
        phoneNumber1 = view.findViewById(R.id.phone1) as EditText
        phoneNumber1?.setText(UserAppPreferences.phoneNumber1)
        callPhone1 = view.findViewById(R.id.callPhone1) as Button
        smsPhone1 = view.findViewById(R.id.smsPhone1) as Button
        setUpContact1()

        name2 = view.findViewById(R.id.name2) as EditText
        name2?.setText(UserAppPreferences.phoneNumber2Name)
        phoneNumber2 = view.findViewById(R.id.phone2) as EditText
        phoneNumber2?.setText(UserAppPreferences.phoneNumber2)
        callPhone2 = view.findViewById(R.id.callPhone2) as Button
        smsPhone2 = view.findViewById(R.id.smsPhone2) as Button
        setUpContact2()

        name3 = view.findViewById(R.id.name3) as EditText
        name3?.setText(UserAppPreferences.phoneNumber3Name)
        phoneNumber3 = view.findViewById(R.id.phone3) as EditText
        phoneNumber3?.setText(UserAppPreferences.phoneNumber3)
        callPhone3 = view.findViewById(R.id.callPhone3) as Button
        smsPhone3 = view.findViewById(R.id.smsPhone3) as Button
        setUpContact3()

        return view
    }

    @SuppressLint("MissingPermission")
    private fun setUpContact1() {

        name1?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (name1!!.text.isEmpty() ) {
                    UserAppPreferences.phoneNumber1Name = ""
                }
                if (name1!!.text.isNotEmpty() && !UserAppPreferences.phoneNumber1Name.equals(name1!!.text.toString())) {
                    UserAppPreferences.phoneNumber1Name = name1!!.text.toString()
                    (activity as MainActivity).sendEmergencyContact(UserAppPreferences.EMERGENCY_PHONE1_NAME.first, UserAppPreferences.phoneNumber1Name)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        phoneNumber1?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (phoneNumber1!!.text.isEmpty() ) {
                    UserAppPreferences.phoneNumber1 = ""
                }
                if (isValidPhone(phoneNumber1!!.text) && !UserAppPreferences.phoneNumber1.equals(phoneNumber1!!.text.toString())) {
                    UserAppPreferences.phoneNumber1 = phoneNumber1!!.text.toString()

                    val bundle = Bundle()
                    bundle.putString(TOTAL_CONTACTS, UserAppPreferences.getTotalContacts().toString())
                    CheckASeniorApp.instance.firebaseAnalytics.logEvent(EMERGENCY_CONTACT1_UPDATED, bundle)

                    (activity as MainActivity).sendEmergencyContact(UserAppPreferences.EMERGENCY_PHONE1.first, UserAppPreferences.phoneNumber1)
                } else {
                    phoneNumber1?.error = "Enter valid number"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        callPhone1?.setOnClickListener {
            callPhone(phoneNumber1!!.text.toString(), true)
            phoneNumberToCall = phoneNumber1!!.text.toString()
            CheckASeniorApp.instance.firebaseAnalytics.logEvent(PHONE_TAPPED, null)

        }

        smsPhone1?.setOnClickListener {
            var message = FirebaseUtils.emergencySMSText
            val tMgr = this@MainActivityFragment.activity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val mPhoneNumber = tMgr.line1Number
            message = message.replace("PLACEHOLDER_PHONE" , mPhoneNumber, false)

            composeSmsMessage(phoneNumber1!!.text.toString(), message)
            CheckASeniorApp.instance.firebaseAnalytics.logEvent(SMS_TAPPED, null)

        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpContact2() {

        name2?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (name2!!.text.isEmpty() ) {
                    UserAppPreferences.phoneNumber2Name = ""
                }

                if (name2!!.text.isNotEmpty() && !UserAppPreferences.phoneNumber2Name.equals(name2!!.text.toString())) {
                    UserAppPreferences.phoneNumber2Name = name2!!.text.toString()
                    (activity as MainActivity).sendEmergencyContact(UserAppPreferences.EMERGENCY_PHONE2_NAME.first, UserAppPreferences.phoneNumber2Name)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        phoneNumber2?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                if (phoneNumber2!!.text.isEmpty() ) {
                    UserAppPreferences.phoneNumber2 = ""
                }

                if (isValidPhone(phoneNumber2!!.text) && !UserAppPreferences.phoneNumber2.equals(phoneNumber2!!.text.toString())) {
                    UserAppPreferences.phoneNumber2 = phoneNumber2!!.text.toString()
                    val bundle = Bundle()
                    bundle.putString(TOTAL_CONTACTS, UserAppPreferences.getTotalContacts().toString())
                    CheckASeniorApp.instance.firebaseAnalytics.logEvent(EMERGENCY_CONTACT2_UPDATED, bundle)

                    (activity as MainActivity).sendEmergencyContact(UserAppPreferences.EMERGENCY_PHONE2.first, UserAppPreferences.phoneNumber2)
                } else {
                    phoneNumber2?.error = "Enter valid number"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        callPhone2?.setOnClickListener {
            callPhone(phoneNumber2!!.text.toString(), true)
            phoneNumberToCall = phoneNumber2!!.text.toString()
            CheckASeniorApp.instance.firebaseAnalytics.logEvent(PHONE_TAPPED, null)

        }

        smsPhone2?.setOnClickListener {
            var message = FirebaseUtils.emergencySMSText
            val tMgr = this@MainActivityFragment.activity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val mPhoneNumber = tMgr.line1Number
            message = message.replace("PLACEHOLDER_PHONE" , mPhoneNumber, false)

            composeSmsMessage(phoneNumber2!!.text.toString(), message)
            CheckASeniorApp.instance.firebaseAnalytics.logEvent(SMS_TAPPED, null)

        }
    }


    private fun setUpContact3() {

        name3?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (name3!!.text.isEmpty() ) {
                    UserAppPreferences.phoneNumber3Name = ""
                }

                if (name3!!.text.isNotEmpty() && !UserAppPreferences.phoneNumber3Name.equals(name3!!.text.toString())) {
                    UserAppPreferences.phoneNumber3Name = name3!!.text.toString()
                    (activity as MainActivity).sendEmergencyContact(UserAppPreferences.EMERGENCY_PHONE3_NAME.first, UserAppPreferences.phoneNumber3Name)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        phoneNumber3?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                if (phoneNumber3!!.text.isEmpty() ) {
                    UserAppPreferences.phoneNumber3 = ""
                }

                if (isValidPhone(phoneNumber3!!.text) && !UserAppPreferences.phoneNumber3.equals(phoneNumber3!!.text.toString())) {
                    UserAppPreferences.phoneNumber3 = phoneNumber3!!.text.toString()
                    val bundle = Bundle()
                    bundle.putString(TOTAL_CONTACTS, UserAppPreferences.getTotalContacts().toString())
                    CheckASeniorApp.instance.firebaseAnalytics.logEvent(EMERGENCY_CONTACT3_UPDATED, bundle)
                    (activity as MainActivity).sendEmergencyContact(UserAppPreferences.EMERGENCY_PHONE3.first, UserAppPreferences.phoneNumber3)
                } else {
                    phoneNumber3?.error = "Enter valid number"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        callPhone3?.setOnClickListener {
            callPhone(phoneNumber3!!.text.toString(), true)
            phoneNumberToCall = phoneNumber3!!.text.toString()
            CheckASeniorApp.instance.firebaseAnalytics.logEvent(PHONE_TAPPED, null)

        }

        smsPhone3?.setOnClickListener {
            var message = FirebaseUtils.emergencySMSText
            val tMgr = this@MainActivityFragment.activity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val mPhoneNumber = tMgr.line1Number
            message = message.replace("PLACEHOLDER_PHONE" , mPhoneNumber, false)

            composeSmsMessage(phoneNumber3!!.text.toString(), message)

            CheckASeniorApp.instance.firebaseAnalytics.logEvent(SMS_TAPPED, null)

        }
    }

    private fun openURL(urls: String, context : Context) {
        val uris = Uri.parse(urls)
        val intents = Intent(Intent.ACTION_VIEW, uris)
        context.startActivity(intents)
    }

    private fun beep(duration: Int) {
        toneG?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, duration)
    }

    private fun callPhone(phone: String, launchDialer: Boolean = false) {
        if (phone.isEmpty()) {
            Toast.makeText(this@MainActivityFragment.activity!!, "Not a valid phone number", Toast.LENGTH_LONG).show()
            return
        }
        if (checkSelfPermission(this@MainActivityFragment.activity!!, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 51)
        } else {
            var intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
            if (launchDialer) {
                intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            }
            startActivity(intent)
        }

    }

    private fun sendSMSToAllEmergencyContacts(){

        val builder = AlertDialog.Builder(this@MainActivityFragment.context)
        builder.setTitle("Sending SMS to emergency contacts")
        var message: String? = null


        if(UserAppPreferences.phoneNumber1.isNotEmpty() && isValidPhone(UserAppPreferences.phoneNumber1)) {
            message = "Reaching out \n ${UserAppPreferences.phoneNumber1Name} @ ${UserAppPreferences.phoneNumber1}"
            sendSMS(UserAppPreferences.phoneNumber1)
        }
        if(UserAppPreferences.phoneNumber2.isNotEmpty() && isValidPhone(UserAppPreferences.phoneNumber2)) {
            message += "\n${UserAppPreferences.phoneNumber2Name} @ ${UserAppPreferences.phoneNumber2} "
            sendSMS(UserAppPreferences.phoneNumber2)
        }
        if(UserAppPreferences.phoneNumber3.isNotEmpty() && isValidPhone(UserAppPreferences.phoneNumber3)) {
            message += "\n${UserAppPreferences.phoneNumber3Name} @ ${UserAppPreferences.phoneNumber3} "
            sendSMS(UserAppPreferences.phoneNumber3)
        }

        builder.setMessage(message)
        val dialog: AlertDialog = builder.create()
        dialog.show()
        Timer().schedule(3000){
            dialog.cancel()
        }



        CheckASeniorApp.instance.firebaseAnalytics.logEvent(EMERGENCY_SMS_SENT, null)

    }

    private fun sendSMS(phone: String) {
        if (phone.isEmpty()) {
            Toast.makeText(this@MainActivityFragment.activity!!, "Not a valid phone number", Toast.LENGTH_LONG).show()
            return
        }
        if (ActivityCompat.checkSelfPermission(this@MainActivityFragment.activity!!, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE), 52)
            }

        } else {
            var message = FirebaseUtils.emergencySMSText
            val tMgr = this@MainActivityFragment.activity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val mPhoneNumber = tMgr.line1Number
            message = message.replace("PLACEHOLDER_PHONE" , mPhoneNumber, false)
            Constants.sendSMS(phone, message, this@MainActivityFragment.activity!!)
        }

    }

    fun composeSmsMessage(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(Uri.parse("smsto:" + phoneNumber)) // This ensures only SMS apps respond
        intent.putExtra("sms_body", message)
        startActivity(intent)
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 51 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callPhone(phoneNumberToCall, true)
        }

        if (requestCode == 52 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            var message = FirebaseUtils.emergencySMSText
            val tMgr = this@MainActivityFragment.activity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val mPhoneNumber = tMgr.line1Number
            message = message.replace("PLACEHOLDER_PHONE" , mPhoneNumber, false)
            composeSmsMessage(phoneNumberToSMS, message)
        }
    }
}
