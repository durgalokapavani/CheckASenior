package com.ben.checkasenior.contacts

import android.app.Fragment
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ben.checkasenior.AppPreferences
import com.ben.checkasenior.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable


class ContactsFragment : Fragment() {

    private var name1: TextView? = null
    private var phoneNumber1: EditText? = null
    private var name2: TextView? = null
    private var phoneNumber2: EditText? = null
    private var name3: TextView? = null
    private var phoneNumber3: EditText? = null

    private var nodesTitle: TextView? = null
    private var nodes: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view =  inflater.inflate(R.layout.fragment_contacts, container, false)
        name1 = view.findViewById(R.id.name1) as TextView
        phoneNumber1 = view.findViewById(R.id.phone1) as EditText

        name2 = view.findViewById(R.id.name2) as TextView
        phoneNumber2 = view.findViewById(R.id.phone2) as EditText

        name3 = view.findViewById(R.id.name3) as TextView
        phoneNumber3 = view.findViewById(R.id.phone3) as EditText

        if(AppPreferences.phoneNumber1Name.isNotEmpty()) {
            name1?.setText(AppPreferences.phoneNumber1Name)
        }
        phoneNumber1?.setText(AppPreferences.phoneNumber1)
        phoneNumber1?.isEnabled =false

        if(AppPreferences.phoneNumber2Name.isNotEmpty()) {
            name2?.setText(AppPreferences.phoneNumber2Name)
        }
        phoneNumber2?.setText(AppPreferences.phoneNumber2)
        phoneNumber2?.isEnabled =false

        if(AppPreferences.phoneNumber3Name.isNotEmpty()) {
            name3?.setText(AppPreferences.phoneNumber3Name)
        }
        phoneNumber3?.setText(AppPreferences.phoneNumber3)
        phoneNumber3?.isEnabled =false


        nodesTitle = view.findViewById(R.id.nodesTitle) as TextView
        nodes = view.findViewById(R.id.nodes) as Button

        val nodesTask: Task<List<Node>> = Wearable.getNodeClient(this@ContactsFragment.activity).connectedNodes
        nodesTask.addOnSuccessListener {
            nodes?.text = it[0].displayName
        }

        return view
    }

    private fun isValidPhone(phone: CharSequence): Boolean {
        return if (TextUtils.isEmpty(phone) || (phone.count() < 9 || phone.count() > 13)) {
            Toast.makeText(this@ContactsFragment.activity, "Phone not valid", Toast.LENGTH_LONG).show()
            false
        } else {
            println("phone: $phone")
            println("VALID: "+ android.util.Patterns.PHONE.matcher(phone).matches())
            android.util.Patterns.PHONE.matcher(phone).matches()
        }
    }
}