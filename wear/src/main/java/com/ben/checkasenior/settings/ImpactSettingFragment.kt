package com.ben.checkasenior.settings

import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.support.wear.widget.SwipeDismissFrameLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ben.checkasenior.AppPreferences
import com.ben.checkasenior.R
import java.math.RoundingMode
import java.text.DecimalFormat




class ImpactSettingFragment : Fragment(), SensorEventListener {

    private var linear: LinearLayout? = null
    private var calibrationButton: Button? = null
    private var impactDesc: TextView? = null

    private var sensorManager: SensorManager? = null
    private var accelerometer : Sensor?= null
    private  var SAMPLING_PERIOD_IN_MICRO_SECONDS = 100000 //0.1 secs
    private var lowG = 1.0
    private var highG = 1.2

    private var lowestG = 0.0
    private var highestG = 0.0

    private val TOTAL_NUMBER_OF_CALIBRATIONS = 5
    private var CURRENT_CALIBRATION = 1

    var listOfLowGs: MutableList<Double> = mutableListOf<Double>()
    var listOfHighGs: MutableList<Double> = mutableListOf<Double>()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_impact, container, false)
        linear = view.findViewById(R.id.linear_container) as LinearLayout
        calibrationButton = view.findViewById(R.id.calibrationButton) as Button
        impactDesc = view.findViewById(R.id.impact_desc) as TextView

        calibrationButton?.setOnClickListener { view ->

            if (canPlayAudio(this.activity)) {
                val mp = MediaPlayer.create(this.activity, R.raw.pass)
                mp.setVolume(0.5f, 0.5f)
                mp.start()
            }

            // get reference of the service
            sensorManager = this.activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            // focus in accelerometer
            accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager!!.registerListener(this, accelerometer,
                    SAMPLING_PERIOD_IN_MICRO_SECONDS, 200000)

            object : CountDownTimer(10000, 1000) {

                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    sensorManager?.unregisterListener(this@ImpactSettingFragment)

                    if (lowG == 0.0 || highG == 0.0 ) {
                        impactDesc?.text = "Error in calibration. Please retry"

                        return
                    }

                    if (CURRENT_CALIBRATION == TOTAL_NUMBER_OF_CALIBRATIONS) {
                        listOfHighGs.add(highG)
                        listOfLowGs.add(lowG)

                        listOfHighGs = listOfHighGs.asSequence().sorted().toMutableList()
                        listOfLowGs = listOfLowGs.asSequence().sorted().toMutableList()
                        print("BEFORE .....")
                        println(listOfHighGs)
                        println(listOfLowGs)
                        //remove bottom outliers
                        listOfHighGs.removeAt(0)
                        listOfLowGs.removeAt(0)
                        //remove top outliers
                        listOfHighGs = listOfHighGs.dropLast(1).toMutableList()
                        listOfLowGs = listOfLowGs.dropLast(1).toMutableList()
                        print("AFTER .....")
                        println(listOfHighGs)
                        println(listOfLowGs)
                        impactDesc?.text = "Calibration completed ${listOfLowGs.last()}(L) ${listOfHighGs.first()}(H)"
                        AppPreferences.highGValue = listOfHighGs.first()
                        AppPreferences.lowGValue = listOfLowGs.last()
                    } else {
                        impactDesc?.text = "Completed $CURRENT_CALIBRATION of $TOTAL_NUMBER_OF_CALIBRATIONS  $lowG(L) $highG(H). Press button. Then drop your watch from 1 ft height onto carpet floor and wait."
                        CURRENT_CALIBRATION++
                        listOfHighGs.add(highG)
                        listOfLowGs.add(lowG)
                        highG = 1.2
                        lowG = 1.0
                    }


                }
            }.start()
        }


        view?.findViewById<SwipeDismissFrameLayout>(R.id.swipe_dismiss_root_impact)?.apply {
            addCallback(object : SwipeDismissFrameLayout.Callback() {

                override fun onDismissed(layout: SwipeDismissFrameLayout) {
                    linear?.visibility = View.GONE
                    layout.visibility = View.GONE
                    fragmentManager.popBackStack()

                }
            })
        }


        return view
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event !=null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {


            val loX = event.values[0].toDouble()
            val loY = event.values[1].toDouble()
            val loZ = event.values[2].toDouble()

            val loAccelerationReader = Math.sqrt(Math.pow(loX, 2.0) + Math.pow(loY, 2.0) + Math.pow(loZ, 2.0))
            val gForce = (loAccelerationReader * ((1.0 * 1000000) / 1000000.0) ) / 9.8

            val df = DecimalFormat("#.#")
            df.roundingMode = RoundingMode.CEILING
            val roundedG = df.format(gForce).toDouble()

            if (roundedG < lowG) lowG = roundedG
            if (roundedG > highG) highG = roundedG

            impactDesc?.text = "Calibrating $CURRENT_CALIBRATION of $TOTAL_NUMBER_OF_CALIBRATIONS... ($roundedG)"


        }
    }

    private fun canPlayAudio(context: Context): Boolean {
        val packageManager = context.packageManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Check whether the device has a speaker.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check FEATURE_AUDIO_OUTPUT to guard against false positives.
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
                return false
            }

            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true
                }
            }
        }
        return false
    }

}