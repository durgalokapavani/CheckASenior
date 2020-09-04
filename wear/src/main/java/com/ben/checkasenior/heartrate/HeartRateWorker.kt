package com.ben.checkasenior.heartrate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.wearable.complications.ProviderUpdateRequester
import android.util.Log
import android.widget.Toast
import androidx.work.Data
import androidx.work.Result
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ben.checkasenior.*
import com.ben.checkasenior.complications.HRComplicationProviderService
import com.ben.checkasenior.home.WatchMainNavActivity
import java.util.concurrent.CountDownLatch


private const val LOG_TAG = "HearRateWorker"

class HeartRateWorker (context : Context, params : WorkerParameters)
    : Worker(context, params), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var hearRateSensor : Sensor ?= null

    var windowStartTime = 0L
    var currentHRValue = 0
    var heartRates: MutableList<Int> = mutableListOf<Int>()
    val countDownLatch = CountDownLatch(1)

    override fun doWork(): Result {
        Log.d(LOG_TAG, "Heart Rate Monitor!")

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING

        if (isCharging) {
            // Return if charging
            return Result.success()
        } else {
            // get reference of the service
            sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

            hearRateSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            sensorManager!!.registerListener(this, hearRateSensor, SensorManager.SENSOR_DELAY_FASTEST)


            try {
                countDownLatch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            //...set the output, and we're done!
            val output = Data.Builder()
                    .putInt(Utils.KEY_HEART_RATE_RESULT, currentHRValue)
                    .build()


            return Result.success(output)

        }

        // (Returning Result.retry() tells WorkManager to try this task again
        // later; Result.failure() says not to try again.)

    }

    private fun sendHRNotification() {
        val notificationId = 101
        // The channel ID of the notification.
        val id = "check_senior_101"

        val mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_LOW

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(id, "Check A Senior", importance)

            // Configure the notification channel.
            mChannel.description = "Check A Senior"

            mChannel.enableLights(true)
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.lightColor = Color.RED

            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

            mNotificationManager.createNotificationChannel(mChannel)

        }


        // Build intent for notification content
        val viewPendingIntent = Intent(applicationContext, WatchMainNavActivity::class.java).let { viewIntent ->
            PendingIntent.getActivity(this.applicationContext, 0, viewIntent, 0)
        }

        // Notification channel ID is ignored for Android 7.1.1
        // (API level 25) and lower.
        val notificationBuilder = NotificationCompat.Builder(this.applicationContext, id)
                .setSmallIcon(R.drawable.ic_cc_settings_button_center)
                .setContentTitle("Heart Rate")
                .setContentText("Your heart rate is ${heartRates.average().toInt()}")
                .setContentIntent(viewPendingIntent)

        NotificationManagerCompat.from(this.applicationContext).apply {
            notify(notificationId, notificationBuilder.build())
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event !=null && event.sensor.type == Sensor.TYPE_HEART_RATE) {

            val heartRateValue : Int = if (event.values.count() > 0) event.values[0].toInt() else 0
            Log.d(LOG_TAG, "Heart Rate $heartRateValue")
            val curTime = System.currentTimeMillis()
            if (heartRateValue > 0 && windowStartTime == 0L) {
                Log.d(LOG_TAG, "Heart Rate Window started!")
                windowStartTime = curTime
            }

            if(windowStartTime > 0L && currentHRValue != heartRateValue) {
                currentHRValue = heartRateValue
                heartRates.add(currentHRValue)
            }

            val diffTime = curTime - windowStartTime
            if (windowStartTime > 0L && diffTime > 60 * 1000) { //60 seconds
                // Stop
                Log.d(LOG_TAG, "Heart Rate Monitor stopped: $heartRates!")
                sensorManager?.unregisterListener(this@HeartRateWorker)
                if (heartRates.count() > 0) {
                    //Toast.makeText(applicationContext, "Sending HR SMS ($heartRate)", Toast.LENGTH_LONG).show()
                    val heartRate = heartRates.average().toInt()
                    AppPreferences.heartRate = heartRate

                    println("Sending HR SMS $heartRate")
                    if(heartRate > FirebaseUtilsWear.highHeartRate) {
                        // High HR
                        Utils.contactEmergencies(context = applicationContext)
                        val bundle = Bundle()
                        bundle.putString("heart_rate", heartRate.toString())
                        App.instance.firebaseAnalytics.logEvent(Utils.HIGH_HR_DETECTED, bundle)

                    }

                    if(heartRate < FirebaseUtilsWear.lowHeartRate) {
                        if (AppPreferences.phoneNumber1.isNotEmpty()) {
                            Utils.contactEmergencies(applicationContext)
                            val bundle = Bundle()
                            bundle.putString("heart_rate", heartRate.toString())
                            App.instance.firebaseAnalytics.logEvent(Utils.LOW_HR_DETECTED, bundle)
                        }

                    }
                    updateComplications()
                    sendHRNotification()
                    countDownLatch.countDown()
                }
            }




        }
    }

    private fun updateComplications() {
        val complicationUpdater = ProviderUpdateRequester(applicationContext, ComponentName(applicationContext, HRComplicationProviderService::class.java))
        complicationUpdater.requestUpdateAll()
    }
}