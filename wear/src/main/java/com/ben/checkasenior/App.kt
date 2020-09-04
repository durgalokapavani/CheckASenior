package com.ben.checkasenior

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.ben.checkasenior.service.FallDetectionService
import com.google.firebase.analytics.FirebaseAnalytics


class App: Application() {

    private val TAG = this::class.java.simpleName

    companion object{

        const val CHANNEL_ID = "checkASeniorChannel"
        lateinit var instance: App
            private set

    }

    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()
        AppPreferences.init(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Check A Senior", NotificationManager.IMPORTANCE_DEFAULT)

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    fun startFDService() {

        val serviceIntent = Intent(this, FallDetectionService::class.java)
        serviceIntent.putExtra("inputExtra", "Fall Detection ACTIVE")

        serviceIntent.putExtra("adjustment", AppPreferences.adjustmentVariable )
        serviceIntent.putExtra("sampling", AppPreferences.SAMPLING_PERIOD_IN_MICRO_SECONDS )
        serviceIntent.putExtra("lowG", AppPreferences.lowGValue)
        serviceIntent.putExtra("highG", AppPreferences.highGValue)
        serviceIntent.putExtra("windowDuration", AppPreferences.WINDOW_DURATION_IN_MILLI_SECONDS)
        serviceIntent.putExtra("impactDuration", AppPreferences.IMPACT_DURATION_IN_MILLI_SECONDS)
        serviceIntent.putExtra("longlieDuration", AppPreferences.LONGLIE_DURATION_IN_MILLI_SECONDS)
        serviceIntent.putExtra("stableLowG", AppPreferences.G_STABLE_RANGE.start)
        serviceIntent.putExtra("stableHighG", AppPreferences.G_STABLE_RANGE.endInclusive)

        ContextCompat.startForegroundService(this, serviceIntent)
        logD(
                TAG,
                "Starting Fall detection service")
    }

    fun stopFDService() {
        val serviceIntent = Intent(this, FallDetectionService::class.java)
        stopService(serviceIntent)
        logD(
                TAG,
                "Stopped Fall detection service")
        Toast.makeText(applicationContext, "Fall detection stopped", Toast.LENGTH_LONG).show()
    }

    /** As simple wrapper around Log.d  */
    private fun logD(tag: String, message: String) {
        //if (Log.isLoggable(tag, Log.DEBUG)) {
        Log.d(tag, message)
        //}
    }
}