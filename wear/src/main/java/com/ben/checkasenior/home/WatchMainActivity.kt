package com.ben.checkasenior.home

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ben.checkasenior.R
import com.ben.checkasenior.service.FallDetectionService
import kotlinx.android.synthetic.main.activity_watch_dev.*


class WatchMainActivity : WearableActivity() {

    private val TAG = this::class.java.simpleName

    internal  val SAMPLING_PERIOD_IN_MICRO_SECONDS = 500000 //0.5 secs
    internal val LOW_G_VALUE = 0.4
    internal val HIGH_G_VALUE = 2.5
    val WINDOW_DURATION_IN_MILLI_SECONDS = 10000 //10 sec
    val IMPACT_DURATION_IN_MILLI_SECONDS = 2000 //2 sec
    val LONGLIE_DURATION_IN_MILLI_SECONDS = 3000 //3 sec
    val G_STABLE_RANGE = 0.7..1.3

    val ADJUSTMENT = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_dev)

        adjustment_var.setText(ADJUSTMENT.toString())
        sampling_frequency.setText((SAMPLING_PERIOD_IN_MICRO_SECONDS / 1000000.0).toString())
        low_g.setText(LOW_G_VALUE.toString())
        high_g.setText(HIGH_G_VALUE.toString())
        window_duration.setText((WINDOW_DURATION_IN_MILLI_SECONDS / 1000.0).toString())
        impact_duration.setText((IMPACT_DURATION_IN_MILLI_SECONDS / 1000.0).toString())
        longlie_duration.setText((LONGLIE_DURATION_IN_MILLI_SECONDS / 1000.0).toString())
        stable_g_low.setText(G_STABLE_RANGE.start .toString())
        stable_g_high.setText(G_STABLE_RANGE.endInclusive.toString())


        // Enables Always-on
        setAmbientEnabled()
    }

    fun startService(v: View) {

        val serviceIntent = Intent(this, FallDetectionService::class.java)
        serviceIntent.putExtra("inputExtra", "Fall Detection ACTIVE")

        serviceIntent.putExtra("adjustment", adjustment_var.text.toString().toDouble() )
        serviceIntent.putExtra("sampling", (sampling_frequency.text.toString().toDouble() * 1000000))
        serviceIntent.putExtra("lowG", low_g.text.toString().toDouble())
        serviceIntent.putExtra("highG", high_g.text.toString().toDouble())
        serviceIntent.putExtra("windowDuration", (window_duration.text.toString().toDouble()) * 1000)
        serviceIntent.putExtra("impactDuration", (impact_duration.text.toString().toDouble()) * 1000)
        serviceIntent.putExtra("longlieDuration", (longlie_duration.text.toString().toDouble()) * 1000)
        serviceIntent.putExtra("stableLowG", stable_g_low.text.toString().toDouble())
        serviceIntent.putExtra("stableHighG", stable_g_high.text.toString().toDouble())

        ContextCompat.startForegroundService(this, serviceIntent)
        logD(
                TAG,
                "Starting Fall detection service")
    }

    fun stopService(v: View) {
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
