package com.ben.checkasenior.falldetection

import android.app.Fragment
import android.os.Bundle
import android.support.wear.widget.BoxInsetLayout
import android.support.wear.widget.SwipeDismissFrameLayout
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.ben.checkasenior.App
import com.ben.checkasenior.AppPreferences
import com.ben.checkasenior.AppPreferences.G_STABLE_RANGE
import com.ben.checkasenior.AppPreferences.IMPACT_DURATION_IN_MILLI_SECONDS
import com.ben.checkasenior.AppPreferences.LONGLIE_DURATION_IN_MILLI_SECONDS
import com.ben.checkasenior.AppPreferences.WINDOW_DURATION_IN_MILLI_SECONDS
import com.ben.checkasenior.R


class FallDetectionFragment : Fragment() {

    private var linear: LinearLayout? = null
    private var scrollViewFD: ScrollView? = null
    private var outerBox: BoxInsetLayout? = null

    private var adjustment_var: EditText? = null
    private  var sampling_frequency: EditText? = null
    private  var low_g: EditText? = null
    private  var high_g: EditText? = null
    private  var window_duration: EditText? = null
    private  var impact_duration: EditText? = null
    private  var longlie_duration: EditText? = null
    private  var stable_g_low: EditText? = null
    private  var stable_g_high: EditText? = null

    private  var startFDButton: Button? = null
    private  var stopFDButton: Button? = null
    


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_fd, container, false)
        linear = view.findViewById(R.id.linearlayout_fd) as LinearLayout
        scrollViewFD = view.findViewById(R.id.scrollview_fd) as ScrollView
        outerBox = view.findViewById(R.id.outer_box_layout) as BoxInsetLayout

        startFDButton = view.findViewById(R.id.startService) as Button
        stopFDButton = view.findViewById(R.id.stopService) as Button

        adjustment_var = view.findViewById(R.id.adjustment_var) as EditText
        sampling_frequency = view.findViewById(R.id.sampling_frequency) as EditText
        low_g = view.findViewById(R.id.low_g) as EditText
        high_g = view.findViewById(R.id.high_g) as EditText
        window_duration = view.findViewById(R.id.window_duration) as EditText
        impact_duration = view.findViewById(R.id.impact_duration) as EditText
        longlie_duration = view.findViewById(R.id.longlie_duration) as EditText
        stable_g_low = view.findViewById(R.id.stable_g_low) as EditText
        stable_g_high = view.findViewById(R.id.stable_g_high) as EditText

        adjustment_var?.setText(AppPreferences.adjustmentVariable.toString())
        adjustment_var?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                AppPreferences.adjustmentVariable = adjustment_var?.text.toString().toDouble()

                return@OnKeyListener false
            }
            false
        })

        low_g?.setText(AppPreferences.lowGValue.toString())
        low_g?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                AppPreferences.lowGValue = low_g?.text.toString().toDouble()

                return@OnKeyListener false
            }
            false
        })

        high_g?.setText(AppPreferences.highGValue.toString())
        high_g?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                AppPreferences.highGValue = high_g?.text.toString().toDouble()

                return@OnKeyListener false
            }
            false
        })

        sampling_frequency?.setText((AppPreferences.SAMPLING_PERIOD_IN_MICRO_SECONDS / 1000000.0).toString())
        //sampling_frequency?.isEnabled = false
        window_duration?.setText((WINDOW_DURATION_IN_MILLI_SECONDS / 1000.0).toString())
        //window_duration?.isEnabled = false
        impact_duration?.setText((IMPACT_DURATION_IN_MILLI_SECONDS / 1000.0).toString())
        //impact_duration?.isEnabled = false
        longlie_duration?.setText((LONGLIE_DURATION_IN_MILLI_SECONDS / 1000.0).toString())
        //longlie_duration?.isEnabled = false
        stable_g_low?.setText(G_STABLE_RANGE.start .toString())
        //stable_g_low?.isEnabled = false
        stable_g_high?.setText(G_STABLE_RANGE.endInclusive.toString())
        //stable_g_high?.isEnabled = false


        view?.findViewById<SwipeDismissFrameLayout>(R.id.swipe_dismiss_root)?.apply {
            addCallback(object : SwipeDismissFrameLayout.Callback() {

                override fun onDismissed(layout: SwipeDismissFrameLayout) {
                    linear?.visibility = View.GONE
                    scrollViewFD?.visibility = View.GONE
                    layout.visibility = View.GONE
                    outerBox?.visibility = View.GONE
                    fragmentManager.popBackStack()

                }
            })
        }

        startFDButton?.setOnClickListener {
            val app = this.activity.application as App
            app.startFDService()
        }

        stopFDButton?.setOnClickListener {
            val app = this.activity.application as App
            app.stopFDService()
        }

        return view
    }
    
}