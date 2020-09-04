package com.ben.checkasenior

import android.content.Context
import android.content.SharedPreferences
import com.ben.checkasenior.Constants.TOTAL_CONTACTS

object UserAppPreferences {
    private const val NAME = "com.ben.checkasenior"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    // list of app specific preferences
    val EMERGENCY_PHONE1 = Pair("EMERGENCY_PHONE1", "")
    val EMERGENCY_PHONE2 = Pair("EMERGENCY_PHONE2", "")
    val EMERGENCY_PHONE3 = Pair("EMERGENCY_PHONE3", "")

    val EMERGENCY_PHONE1_NAME = Pair("EMERGENCY_PHONE1_NAME", "")
    val EMERGENCY_PHONE2_NAME = Pair("EMERGENCY_PHONE2_NAME", "")
    val EMERGENCY_PHONE3_NAME = Pair("EMERGENCY_PHONE3_NAME", "")

    fun getTotalContacts(): Int {
        var count:Int = 0
        if(phoneNumber1.isNotEmpty()) {
            count++
        }
        if(phoneNumber2.isNotEmpty()) {
            count++
        }
        if(phoneNumber3.isNotEmpty()) {
            count++
        }

        CheckASeniorApp.instance.firebaseAnalytics.setUserProperty(TOTAL_CONTACTS, count.toString())
        return count
    }

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var phoneNumber1: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(EMERGENCY_PHONE1.first, EMERGENCY_PHONE1.second)

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(EMERGENCY_PHONE1.first, value)
        }

    var phoneNumber2: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(EMERGENCY_PHONE2.first, EMERGENCY_PHONE2.second)

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(EMERGENCY_PHONE2.first, value)
        }

    var phoneNumber3: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(EMERGENCY_PHONE3.first, EMERGENCY_PHONE3.second)

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(EMERGENCY_PHONE3.first, value)
        }

    var phoneNumber1Name: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(EMERGENCY_PHONE1_NAME.first, EMERGENCY_PHONE1_NAME.second)

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(EMERGENCY_PHONE1_NAME.first, value)
        }

    var phoneNumber2Name: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(EMERGENCY_PHONE2_NAME.first, EMERGENCY_PHONE2_NAME.second)

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(EMERGENCY_PHONE2_NAME.first, value)
        }

    var phoneNumber3Name: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(EMERGENCY_PHONE3_NAME.first, EMERGENCY_PHONE3_NAME.second)

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(EMERGENCY_PHONE3_NAME.first, value)
        }


}