package com.ben.checkasenior.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.ben.checkasenior.App
import com.ben.checkasenior.AppPreferences
import com.ben.checkasenior.AppPreferences.G_STABLE_RANGE
import com.ben.checkasenior.AppPreferences.IMPACT_DURATION_IN_MILLI_SECONDS
import com.ben.checkasenior.AppPreferences.LONGLIE_DURATION_IN_MILLI_SECONDS
import com.ben.checkasenior.AppPreferences.SAMPLING_PERIOD_IN_MICRO_SECONDS
import com.ben.checkasenior.AppPreferences.WINDOW_DURATION_IN_MILLI_SECONDS
import com.ben.checkasenior.R
import com.ben.checkasenior.Utils
import com.ben.checkasenior.Utils.FALL_DETECTED
import com.ben.checkasenior.home.WatchMainNavActivity
import java.math.RoundingMode
import java.text.DecimalFormat


class FallDetectionService : Service(), SensorEventListener {

    private val TAG = this::class.java.simpleName

    private var sensorManager: SensorManager? = null
    private var accelerometer : Sensor ?= null

    internal var fall = false


    /*internal  var SAMPLING_PERIOD_IN_MICRO_SECONDS = 500000 //0.5 secs
    internal var LOW_G_VALUE = 0.4
    internal var HIGH_G_VALUE = 2.5
    var WINDOW_DURATION_IN_MILLI_SECONDS = 10000 //10 sec
    var IMPACT_DURATION_IN_MILLI_SECONDS = 2000 //2 sec
    var LONGLIE_DURATION_IN_MILLI_SECONDS = 3000 //3 sec
    var G_STABLE_RANGE = 0.7..1.3
    var ADJUSTMENT = 1.0*/

    enum class FallPhase {
        PREFALL, FALL, LONGLIE, NONE
    }
    var currentState = FallPhase.NONE
    var windowStartTime = 0L
    var fallingStartTime = 0L
    var impactTime = 0L
    var longLieStartTime = 0L

    var impactG = 0.0

    override fun onBind(intent: Intent?): IBinder = throw UnsupportedOperationException("Not yet implemented")

    override fun onCreate() {
        super.onCreate()

        // get reference of the service
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager!!.registerListener(this, accelerometer,
                SAMPLING_PERIOD_IN_MICRO_SECONDS, 500000)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("inputExtra")

        /*ADJUSTMENT = intent!!.getDoubleExtra("adjustment", 1.0)
        SAMPLING_PERIOD_IN_MICRO_SECONDS = intent!!.getDoubleExtra("sampling", 500000.0).toInt()
        LOW_G_VALUE = intent!!.getDoubleExtra("lowG", 0.4)
        HIGH_G_VALUE = intent!!.getDoubleExtra("highG", 2.5)
        WINDOW_DURATION_IN_MILLI_SECONDS = intent!!.getDoubleExtra("windowDuration", 10000.0).toInt()
        IMPACT_DURATION_IN_MILLI_SECONDS = intent!!.getDoubleExtra("impactDuration", 2000.0).toInt()
        LONGLIE_DURATION_IN_MILLI_SECONDS = intent!!.getDoubleExtra("longlieDuration", 3000.0).toInt()
        val start = intent!!.getDoubleExtra("stableLowG", 0.7)
        val end = intent!!.getDoubleExtra("stableHighG", 1.3)
        G_STABLE_RANGE = start..end*/

        //Log.d(TAG, "IMPACT_DURATION_IN_MILLI_SECONDS start: $IMPACT_DURATION_IN_MILLI_SECONDS")
        //Log.d(TAG, "LOW_G_VALUE : $LOW_G_VALUE")

        val notificationIntent = Intent(this, WatchMainNavActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Check A Senior").setContentText(input)
                .setSmallIcon(R.drawable.ic_cc_settings_button_center)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(199, notification)

        //do heavy work on a background thread
        //stopSelf();
        Toast.makeText(applicationContext, "Fall detection started", Toast.LENGTH_LONG).show()

        return Service.START_NOT_STICKY
    }

    /*
     * Reset
     */
    internal  fun reset() {
        currentState = FallPhase.NONE
        windowStartTime = 0L
        longLieStartTime = 0L
        fallingStartTime = 0L
        impactTime = 0L
        fall = false
        impactG = 0.0
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event !=null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {


            val loX = event.values[0].toDouble()
            val loY = event.values[1].toDouble()
            val loZ = event.values[2].toDouble()

            val loAccelerationReader = Math.sqrt(Math.pow(loX, 2.0) + Math.pow(loY, 2.0) + Math.pow(loZ, 2.0))
            val gForce = (loAccelerationReader * ((AppPreferences.adjustmentVariable * 1000000) / 1000000.0) ) / 9.8

            val curTime = System.currentTimeMillis()

            //Log.d(TAG, "X Y X: $loX, $loY, $loZ")
            //Log.d(TAG, "loAccelerationReader VALUE: $loAccelerationReader")
            //Log.d(TAG, "G FORCE VALUE: $gForce")

            //if not in window, reset
            val diffTime = curTime - windowStartTime
            if (windowStartTime > 0 && diffTime > WINDOW_DURATION_IN_MILLI_SECONDS) { //with in window
                Log.d(TAG, "WINDOW RESET!!!")
                reset()

            }

            // The below code executes within the window
            when(currentState) {
                FallPhase.NONE -> {
                    if (gForce < AppPreferences.lowGValue) {
                        //start the fall detection window
                        currentState = FallPhase.PREFALL
                        windowStartTime = curTime
                        fallingStartTime = curTime
                        Log.d(TAG, "Pre-fall started at: $curTime")
                        Log.d(TAG, "G FORCE VALUE, LOW_G: $gForce, $AppPreferences.lowGValue")
                    }
                }

                FallPhase.PREFALL -> {
                    val diffTime = curTime - windowStartTime
                    val fallDiffTime = curTime - fallingStartTime
                    if (fallDiffTime > IMPACT_DURATION_IN_MILLI_SECONDS) {
                        // impact did not happen in given duration, so reset
                        Log.d(TAG, "NO Impact, RESET!!!")
                        Log.d(TAG, "G FORCE VALUE: $gForce")
                        reset()
                    }
                    // Check if gforce reached high G value
                    if (gForce > AppPreferences.highGValue ) {
                        Log.d(TAG, "Impact happened at: $curTime")
                        Log.d(TAG, "G FORCE VALUE: $gForce")
                        impactG = gForce
                        currentState = FallPhase.FALL
                        impactTime = curTime

                        val df = DecimalFormat("#.##")
                        df.roundingMode = RoundingMode.CEILING
                        val roundedImpact = df.format(impactG).toDouble()
                        Toast.makeText(applicationContext, "IMPACT $roundedImpact", Toast.LENGTH_LONG).show()
                    }

                }

                FallPhase.FALL -> {
                    val diffTime = curTime - impactTime
                    if (gForce in G_STABLE_RANGE &&
                            diffTime > IMPACT_DURATION_IN_MILLI_SECONDS) {
                        Log.d(TAG, "Long lie started at: $curTime")
                        Log.d(TAG, "G FORCE VALUE: $gForce")
                        currentState = FallPhase.LONGLIE
                        longLieStartTime = curTime

                    }

                }

                FallPhase.LONGLIE -> {
                    val longLieTime = curTime - longLieStartTime
                    if (longLieTime > LONGLIE_DURATION_IN_MILLI_SECONDS) {
                        Log.d(TAG, "Long lie duration: $longLieTime")
                        Log.d(TAG, "FALL DETECTED !!!")
                        fall = true

                    }
                    if (gForce in G_STABLE_RANGE) {
                        //Log.d(TAG, "G FORCE VALUE: $gForce")
                        currentState = FallPhase.LONGLIE

                    } else {
                        // G value is not in range, user might have recovered, so reset
                        Log.d(TAG, "G value not in LONGLIE range, RESET!!!")
                        Log.d(TAG, "G FORCE VALUE: $gForce")
                        val df = DecimalFormat("#.##")
                        df.roundingMode = RoundingMode.CEILING
                        val roundedG = df.format(gForce).toDouble()
                        Toast.makeText(applicationContext, "RESET $roundedG", Toast.LENGTH_LONG).show()
                        reset()
                    }

                }
            }


            if(fall) {
                Log.d(TAG, "FALL detected")
                val bundle = Bundle()
                bundle.putString("impact_g", impactG.toString())
                App.instance.firebaseAnalytics.logEvent(FALL_DETECTED, bundle)
                Utils.contactEmergencies(this)
                val serviceIntent = Intent(this, FallDetectionService::class.java)
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.CEILING
                val roundedImpact = df.format(impactG).toDouble()
                serviceIntent.putExtra("inputExtra", "Fall Detected \n impact G: $roundedImpact")

                ContextCompat.startForegroundService(this, serviceIntent)

                fall = false
                reset()

            }

        }
    }

}