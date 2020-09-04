package com.ben.checkasenior.home

import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.support.wear.widget.CircularProgressLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ben.checkasenior.App
import com.ben.checkasenior.R
import com.ben.checkasenior.Utils.SOS_CANCELLED_WATCH
import com.ben.checkasenior.Utils.SOS_TAPPED_WATCH
import com.ben.checkasenior.contacts.ContactHelpFragment


class SOSFragment : Fragment(), CircularProgressLayout.OnTimerFinishedListener,
        View.OnClickListener {
    private var sosImageView: ImageView? = null
    private var sosTextView: TextView? = null
    private var mCircularProgress: CircularProgressLayout? = null
    var toneG: ToneGenerator? = null
    var packageManager: PackageManager? = null
    var audioManager: AudioManager? = null
    var countDownTimer:CountDownTimer? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_sos, container, false)

        packageManager = this.activity.packageManager
        audioManager = this.activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        toneG = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 50)

        sosImageView = view.findViewById(R.id.sos_image_view) as ImageView
        sosTextView = view.findViewById(R.id.sos_text_view) as TextView

        mCircularProgress = view. findViewById<CircularProgressLayout>(R.id.circular_progress).apply {
            onTimerFinishedListener = this@SOSFragment
            setOnClickListener(this@SOSFragment)
        }

        return view
    }

    override fun onTimerFinished(layout: CircularProgressLayout) {
        // User didn't cancel, perform the action
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val helpFragment = ContactHelpFragment()
        fragmentTransaction.add(R.id.fragment_container, helpFragment)
        fragmentTransaction.commit()

        mCircularProgress?.stopTimer()
        sosTextView?.text = "SOS"
    }

    override fun onClick(view: View) {
        if(mCircularProgress!!.isTimerRunning) {
            // User canceled, abort the action
            App.instance.firebaseAnalytics.logEvent(SOS_CANCELLED_WATCH, null)
            mCircularProgress?.stopTimer()
            sosTextView?.text = "SOS"
            toneG?.stopTone()
            countDownTimer?.cancel()

        } else {
            App.instance.firebaseAnalytics.logEvent(SOS_TAPPED_WATCH, null)
            mCircularProgress?.apply {
                sosTextView?.text = "X"
                // 5 seconds to cancel the action
                totalTime = 5000
                // Start the timer
                startTimer()
                countDownTimer = object : CountDownTimer(totalTime, totalTime/5) {

                    override fun onTick(millisUntilFinished: Long) {
                        //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000)
                        if (deviceHasSpeaker()) {
                            beep(300)
                        }
                    }

                    override fun onFinish() {}
                }.start()


            }

        }



    }

    private fun deviceHasSpeaker() : Boolean {
        // Check whether the device has a speaker.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                // Check FEATURE_AUDIO_OUTPUT to guard against false positives.
                packageManager!!.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            val devices: Array<AudioDeviceInfo> = audioManager!!.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
        } else {
            false
        }
    }

    private fun beep(duration: Int) {
        toneG?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, duration)
//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
//            toneG?.release()
//        }, (duration + 50).toLong())
    }

    override fun onDestroy() {
        super.onDestroy()
        toneG?.release()
    }
}