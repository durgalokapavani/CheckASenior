package com.ben.checkasenior

import android.content.Context
import android.content.SharedPreferences
import java.math.RoundingMode
import java.text.DecimalFormat

object AppPreferences {
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

    private val ADJUSTMENT_VAR = Pair("ADJUSTMENT_VAR", 1.0)
    private val LOW_G_VALUE = Pair("LOW_G_VALUE", 0.4)
    private val HIGH_G_VALUE = Pair("HIGH_G_VALUE", 3.5)
    private val HEART_RATE_VALUE = Pair("HEART_RATE_VALUE", 72)

    val SAMPLING_PERIOD_IN_MICRO_SECONDS = 500000 //0.5 secs
    val WINDOW_DURATION_IN_MILLI_SECONDS = 10000 //10 sec
    val IMPACT_DURATION_IN_MILLI_SECONDS = 2000 //2 sec
    val LONGLIE_DURATION_IN_MILLI_SECONDS = 5000 //5 sec
    val G_STABLE_RANGE = 0.7..1.3

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

    var adjustmentVariable: Double
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = roundValue(preferences.getFloat(ADJUSTMENT_VAR.first, ADJUSTMENT_VAR.second.toFloat()).toDouble())

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putFloat(ADJUSTMENT_VAR.first, value.toFloat())
        }

    var lowGValue: Double
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = roundValue(preferences.getFloat(LOW_G_VALUE.first, LOW_G_VALUE.second.toFloat()).toDouble())

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putFloat(LOW_G_VALUE.first, value.toFloat())
        }

    var highGValue: Double
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = roundValue(preferences.getFloat(HIGH_G_VALUE.first, HIGH_G_VALUE.second.toFloat()).toDouble())

        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putFloat(HIGH_G_VALUE.first, value.toFloat())
        }

    var heartRate: Int
        get() = preferences.getInt(HEART_RATE_VALUE.first, HEART_RATE_VALUE.second)

        set(value) = preferences.edit {
            it.putInt(HEART_RATE_VALUE.first, value)
        }

    fun roundValue(value: Double): Double {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.DOWN
        return df.format(value).toDouble()
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