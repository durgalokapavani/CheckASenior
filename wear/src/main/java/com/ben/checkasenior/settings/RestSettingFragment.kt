package com.ben.checkasenior.settings

import android.app.Fragment
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wear.widget.SwipeDismissFrameLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ben.checkasenior.AppPreferences
import com.ben.checkasenior.R
import java.math.RoundingMode
import java.text.DecimalFormat




class RestSettingFragment : Fragment(), SensorEventListener {

    private var linear: LinearLayout? = null
    private  var restGValue: TextView? = null
    private  var restCalibration: TextView? = null

    private var sensorManager: SensorManager? = null
    private var accelerometer : Sensor?= null
    private  var SAMPLING_PERIOD_IN_MICRO_SECONDS = 100000 //0.1 secs
    private var prevRoundedG = 1.0
    private var gIsStable = false
    var stabilityStartTime = 0L
    var STABILITY_DURATION_IN_MILLI_SECONDS = 3000 //3 sec


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_rest, container, false)
        linear = view.findViewById(R.id.linear_container) as LinearLayout
        restGValue = view.findViewById(R.id.restGValue) as TextView
        restCalibration = view.findViewById(R.id.restGValue) as TextView

        // get reference of the service
        sensorManager = this.activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager!!.registerListener(this, accelerometer,
                SAMPLING_PERIOD_IN_MICRO_SECONDS, 200000)

        view?.findViewById<SwipeDismissFrameLayout>(R.id.swipe_dismiss_root)?.apply {
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

            val curTime = System.currentTimeMillis()
            val df = DecimalFormat("#.#")
            df.roundingMode = RoundingMode.CEILING
            val roundedG = df.format(gForce).toDouble()

            var delta = Math.abs(prevRoundedG - roundedG)
            delta = df.format(delta).toDouble()
            restGValue?.text = "$roundedG G, $delta"

            if (delta > 0) {
                gIsStable = false
                stabilityStartTime = 0L
            }

            if (gIsStable) {
                val stabilityTime = curTime - stabilityStartTime
                if (stabilityTime > STABILITY_DURATION_IN_MILLI_SECONDS) {
                    sensorManager?.unregisterListener(this)

                    var adjustmentValue = 1.0/roundedG
                    adjustmentValue = df.format(adjustmentValue).toDouble()
                    restCalibration?.text = "Success $adjustmentValue(A) $roundedG(S)"
                    AppPreferences.adjustmentVariable = adjustmentValue

                }
            } else if (delta == 0.0) {
                gIsStable = true
                stabilityStartTime = curTime

            }
            prevRoundedG = roundedG



        }
    }
}