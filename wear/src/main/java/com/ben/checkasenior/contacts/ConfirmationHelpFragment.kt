package com.ben.checkasenior.contacts

import android.app.Fragment
import android.os.Bundle
import android.support.wear.widget.SwipeDismissFrameLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ben.checkasenior.R


class ConfirmationHelpFragment : Fragment() {

    private var linear: LinearLayout? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_help_confirmation, container, false)
        linear = view.findViewById(R.id.linear_container) as LinearLayout
        view?.findViewById<SwipeDismissFrameLayout>(R.id.confirmation_swipe_dismiss_root)?.apply {
            addCallback(object : SwipeDismissFrameLayout.Callback() {

                override fun onDismissed(layout: SwipeDismissFrameLayout) {
                    linear?.visibility = View.GONE
                    layout.visibility = View.GONE
                    fragmentManager.popBackStack()

                }
            })
        }


        return view
    }
}