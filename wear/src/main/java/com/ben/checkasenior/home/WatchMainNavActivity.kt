package com.ben.checkasenior.home

import android.Manifest
import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ben.checkasenior.App
import com.ben.checkasenior.FirebaseUtilsWear
import com.ben.checkasenior.NavigationAdapter
import com.ben.checkasenior.R
import com.ben.checkasenior.R.id.top_navigation_drawer
import com.ben.checkasenior.contacts.ContactsFragment
import com.ben.checkasenior.heartrate.HeartRateWorker
import com.ben.checkasenior.settings.SettingsFragment
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.android.synthetic.main.activity_watch_main.*
import java.util.concurrent.TimeUnit




class WatchMainNavActivity : WearableActivity(), FirebaseUtilsWear.OnFirebaseUtilListener  {

    private val TAG = this::class.java.simpleName

    private var mCurrentSection = Section.SOS

    var isActivityDestroyed: Boolean = false
    private var firebaseUtils: FirebaseUtilsWear? = null

    private

    companion object {
        val EXTRA_SECTION = "com.ben.checkasenior.navaction.EXTRA_SECTION"
        /**
         * Helper method to quickly create sections.
         *
         * @param section The [Section] to use.
         * @return A new SOSFragment with arguments set based on the provided Section.
         */
        fun getSection(section: Section): Fragment {
            return when {
                section === Section.SOS -> SOSFragment()
                section === Section.CONTACTS -> ContactsFragment()
                section === Section.SETTINGS -> SettingsFragment()
                else -> SOSFragment()
            }
        }
    }

    enum class Section constructor(internal val titleRes: Int, internal val drawableRes: Int) {
        SOS(R.string.sos_title, R.drawable.ic_local_sos_black_24dp),
        CONTACTS(R.string.contacts_title, R.drawable.ic_contacts_black_24dp),
        SETTINGS(R.string.settings_title, R.drawable.ic_settings_black_24dp)
    }

    override fun onResume() {
        super.onResume()
        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)

        val nodesTask:Task<List<com.google.android.gms.wearable.Node>> = Wearable.getNodeClient(this).connectedNodes
        nodesTask.addOnSuccessListener {
            it.forEach { node ->
                val sendTask: Task<*> = Wearable.getMessageClient(this).sendMessage(
                        node.id,
                        "/WATCH_APP_OPENED",
                        "WATCH_APP_OPENED".toByteArray()
                ).apply {
                    addOnSuccessListener {
                        Log.d(TAG, "Start up Message sent: $it")
                    }
                    addOnFailureListener {  }
                }

            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_main)

        getRemoteValues()

        top_navigation_drawer.setAdapter(NavigationAdapter(this))
        top_navigation_drawer.controller.peekDrawer()
        top_navigation_drawer.addOnItemSelectedListener {
            val selectedSection = Section.values()[it]

            // Only replace the fragment if the section is changing.
            if (selectedSection != mCurrentSection) {
                mCurrentSection = selectedSection

                val sectionFragment = getSection(selectedSection)
                fragmentManager.beginTransaction().replace(R.id.fragment_container, sectionFragment).commit()
            }



        }
        val sunSection = getSection(Section.SOS)
        fragmentManager.beginTransaction().replace(R.id.fragment_container, sunSection).commit()

        checkSMSPermission()
        // Enables Always-on
        setAmbientEnabled()


        if (checkSelfPermission(Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 101)
        } else {
            //Your code for declaring and initializing Sensor manager and Heartrate sensor
            startHRWorker()
        }

        val app = this.application as App
        app.startFDService()
    }

    override fun onFirebaseConfigReceived(isSuccess: Boolean) {
        if (isActivityDestroyed) return
        firebaseUtils?.extractValues()
    }

    override fun onDestroy() {
        isActivityDestroyed = true
        super.onDestroy()
    }

    private fun startHRWorker() {
        val heartRateBuilder = PeriodicWorkRequest.Builder(HeartRateWorker::class.java, 16, TimeUnit.MINUTES)

        // Create the actual work object:
        val heartRateWork = heartRateBuilder.build()
        // Then enqueue the recurring task:
        WorkManager.getInstance().enqueueUniquePeriodicWork("CheckASeniorHRJob", ExistingPeriodicWorkPolicy.KEEP, heartRateWork);
        //val request = OneTimeWorkRequest.Builder(HeartRateWorker::class.java).build()
        //WorkManager.getInstance().enqueue(request)

        Toast.makeText(applicationContext, "HR monitor scheduled", Toast.LENGTH_LONG).show()
    }
    /** As simple wrapper around Log.d  */
    private fun logD(tag: String, message: String) {
        //if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message)
        //}
    }

    private fun checkSMSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE), 10)
            }

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startHRWorker()
        }
    }


    private fun getRemoteValues() {
        firebaseUtils = FirebaseUtilsWear()
        firebaseUtils?.setOnFirebaseUtilListener(this)
        firebaseUtils?.fetch()
    }

}
