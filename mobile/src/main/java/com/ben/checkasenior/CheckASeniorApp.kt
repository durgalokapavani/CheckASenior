package com.ben.checkasenior

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

class CheckASeniorApp : Application() {
    private val TAG = this::class.java.simpleName

    companion object{
        lateinit var instance: CheckASeniorApp
            private set

    }

    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        instance = this
        UserAppPreferences.init(this)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }
}