package com.ben.checkasenior.settings

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.ben.checkasenior.R
import com.ben.checkasenior.falldetection.FallDetectionFragment


class SettingsFragment : Fragment() {

    private var restButton: Button? = null
    private var impactButton: Button? = null
    private var fallButton: Button? = null
    private var imageViewImpact:ImageView? = null
    private var count_tap = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_settings, container, false)
        restButton = view.findViewById(R.id.rest_button) as Button
        impactButton = view.findViewById(R.id.impact_button) as Button
        fallButton = view.findViewById(R.id.fallDetectionbutton) as Button
        imageViewImpact = view.findViewById(R.id.imageViewImpact) as ImageView

        impactButton?.visibility = View.INVISIBLE
        fallButton?.visibility = View.INVISIBLE
        imageViewImpact?.visibility = View.INVISIBLE

        view.setOnClickListener {
            count_tap++

            if (count_tap == 10) {
                impactButton?.visibility = View.VISIBLE
                fallButton?.visibility = View.VISIBLE
                imageViewImpact?.visibility = View.VISIBLE
                Toast.makeText(activity.applicationContext, "Enabling developer settings", Toast.LENGTH_LONG).show()
            }
        }

        restButton?.setOnClickListener {
            val fragmentManager = fragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val restFragment = RestSettingFragment()
            fragmentTransaction.add(R.id.fragment_container, restFragment)
            fragmentTransaction.commit()
        }

        impactButton?.setOnClickListener {
            val fragmentManager = fragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val impactFragment = ImpactSettingFragment()
            fragmentTransaction.add(R.id.fragment_container, impactFragment)
            fragmentTransaction.commit()
        }

        fallButton?.setOnClickListener {
            val fragmentManager = fragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val fallFragment = FallDetectionFragment()
            fragmentTransaction.add(R.id.fragment_container, fallFragment)
            fragmentTransaction.commit()
        }

        return view
    }
}