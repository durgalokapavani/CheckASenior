package com.ben.checkasenior

import android.app.Activity
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.*


class FirebaseUtilsWear {

    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var cacheExpiration: Long = 3600 // 1 hour in seconds
    private var listener: OnFirebaseUtilListener? = null

    companion object {
        private val TAG = FirebaseUtilsWear::class.java.simpleName

        //Keys of all the values
        private const val EMERGENCY_SMS_TEXT = "emergency_sms_text"
        private const val SUBSCRIPTION_URL = "subscription_url"
        private const val LOW_HEART_RATE = "low_heart_rate"
        private const val HIGHT_HEART_RATE = "high_heart_rate"

        var emergencySMSText = "I need emergency help. Please call me immediately at PLACEHOLDER_PHONE. This message was sent via the app CheckASenior."
        var subscriptionURL = "http://checkasenior.com"
        var lowHeartRate:Long = 50
        var highHeartRate:Long = 130
    }

    /**
     * set default values for remote config
     *
     * @return map of default values
     */
    private val defaultValues: Map<String, Any>
        get() {
            val map = HashMap<String, Any>()
            map[EMERGENCY_SMS_TEXT] = emergencySMSText
            map[SUBSCRIPTION_URL] = subscriptionURL
            map[LOW_HEART_RATE] = lowHeartRate
            map[HIGHT_HEART_RATE] = highHeartRate

            return map
        }

    private val mFailureListener = OnFailureListener { e ->
        val extraData = HashMap<String, Any>()
        //            extraData.put(AnalyticsManager.ErrorKey.error_description, new Gson().toJson(e));
        //            AnalyticsManager.getInstance().trackError(AnalyticsManager.ErrorKey.REMOTE_CONFIG_FETCH_FAILURE, extraData);

        if (listener != null) {
            listener!!.onFirebaseConfigReceived(false)
        }
        e.printStackTrace()
    }


    /**
     * to getScoreData the firebase remote config
     */
    private fun initialize() {
        var isDeveloperMode = false
        if (BuildConfig.BUILD_TYPE.equals("debug", ignoreCase = true)) {
            isDeveloperMode = true
        }
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(isDeveloperMode) //this will be false in release
                .build()
        mFirebaseRemoteConfig!!.setConfigSettings(configSettings)
        mFirebaseRemoteConfig!!.setDefaults(defaultValues)
    }

    /**
     * too fetch the values from firebase server
     */
    fun fetch() {

        if (mFirebaseRemoteConfig == null) {
            initialize()
        }

        if (mFirebaseRemoteConfig!!.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }

        mFirebaseRemoteConfig!!.fetch(cacheExpiration)
                .addOnFailureListener(mFailureListener)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        // After config data is successfully fetched, it must be activated before newly fetched
                        // values are returned.
                        mFirebaseRemoteConfig!!.activateFetched()
                        if (listener != null) {
                            listener!!.onFirebaseConfigReceived(true)
                        }
                    }
                }
    }//end of fetch

    /**
     * getting the values from firebase config object
     */
    fun extractValues() {
        emergencySMSText = mFirebaseRemoteConfig!!.getString(EMERGENCY_SMS_TEXT)
        subscriptionURL = mFirebaseRemoteConfig!!.getString(SUBSCRIPTION_URL)
        lowHeartRate = mFirebaseRemoteConfig!!.getLong(LOW_HEART_RATE)
        highHeartRate = mFirebaseRemoteConfig!!.getLong(HIGHT_HEART_RATE)
        println(subscriptionURL)

    }

    fun setOnFirebaseUtilListener(activity: Activity) {
        this.listener = activity as OnFirebaseUtilListener
    }

    interface OnFirebaseUtilListener {
        fun onFirebaseConfigReceived(isSuccess: Boolean)
    }
}
