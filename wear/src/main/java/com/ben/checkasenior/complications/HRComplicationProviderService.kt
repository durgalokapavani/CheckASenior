package com.ben.checkasenior.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import android.util.Log
import com.ben.checkasenior.AppPreferences
import com.ben.checkasenior.R
import com.ben.checkasenior.home.WatchMainNavActivity


private const val TAG = "HRProviderService"

class HRComplicationProviderService : ComplicationProviderService() {

    /*
     * Called when the complication needs updated data from your provider. There are four scenarios
     * when this will happen:
     *
     *   1. An active watch face complication is changed to use this provider
     *   2. A complication using this provider becomes active
     *   3. The period of time you specified in the manifest has elapsed (UPDATE_PERIOD_SECONDS)
     *   4. You triggered an update from your own class via the
     *       ProviderUpdateRequester.requestUpdate() method.
     */
    override fun onComplicationUpdate(complicationId: Int, dataType: Int, complicationManager: ComplicationManager) {
        Log.d(TAG, "onComplicationUpdate() id: $complicationId")


        val hearRate = AppPreferences.heartRate.toString()
        when (dataType) {
            ComplicationData.TYPE_SHORT_TEXT ->
                ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                        .setShortText(ComplicationText.plainText(hearRate))
                        .setIcon(Icon.createWithResource(this, R.drawable.ic_heart_white_48px))
                        .setTapAction(createMainActivityIntent())
                        .build().also { complicationData ->
                            complicationManager.updateComplicationData(complicationId, complicationData)
                        }
            ComplicationData.TYPE_LONG_TEXT ->
                ComplicationData.Builder(ComplicationData.TYPE_LONG_TEXT)
                        .setLongText(ComplicationText.plainText("Heart rate is $hearRate"))
                        .setLongTitle(ComplicationText.plainText("CheckASenior"))
                        .setIcon(Icon.createWithResource(this, R.drawable.ic_heart_white_48px))
                        .setTapAction(createMainActivityIntent())
                        .build().also { complicationData ->
                            complicationManager.updateComplicationData(complicationId, complicationData)
                        }
            //Examples: https://www.programcreek.com/java-api-examples/?class=android.support.wearable.complications.ComplicationManager&method=updateComplicationData
            ComplicationData.TYPE_SMALL_IMAGE ->
                ComplicationData.Builder(ComplicationData.TYPE_SMALL_IMAGE)
                        .setSmallImage(Icon.createWithResource(this, R.drawable.ic_heart_white_48px))
                        .setTapAction(createMainActivityIntent())
                        .build().also { complicationData ->
                            complicationManager.updateComplicationData(complicationId, complicationData)
                        }
            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type $dataType")
                }
                // If no data is sent, we still need to inform the ComplicationManager, so
                // the update job can finish and the wake lock isn't held any longer.
                complicationManager.noUpdateRequired(complicationId)
            }
        }

    }

    private fun createMainActivityIntent(): PendingIntent {
        val intent = Intent(this, WatchMainNavActivity::class.java)
        return PendingIntent.getActivity(this, 201, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}