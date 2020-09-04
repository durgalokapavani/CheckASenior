package com.ben.checkasenior

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.android.synthetic.main.activity_main.*




class MainActivity : AppCompatActivity(), FirebaseUtils.OnFirebaseUtilListener  {

    private val TAG = this::class.java.simpleName

    //private lateinit var client: GoogleApiClient
    //private var connectedNode: List<Node>? = null
    // Check here for Message/Data passing
    // https://github.com/googlesamples/android-DataLayer/blob/master/Application/src/main/java/com/example/android/wearable/datalayer/MainActivity.java

    var isActivityDestroyed: Boolean = false
    private var firebaseUtils: FirebaseUtils? = null

    private fun getRemoteValues() {
        firebaseUtils = FirebaseUtils()
        firebaseUtils?.setOnFirebaseUtilListener(this)
        firebaseUtils?.fetch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        getRemoteValues()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE), 52)
            }

        }
    }


    override fun onFirebaseConfigReceived(isSuccess: Boolean) {
        if (isActivityDestroyed) return
        firebaseUtils?.extractValues()
    }

    override fun onDestroy() {
        isActivityDestroyed = true
        super.onDestroy()
    }


    /** As simple wrapper around Log.d  */
    private fun logD(tag: String, message: String) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message)
        }
    }

    fun sendEmergencyContact(key: String, value: String) {
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/emergencyContacts").run {
            dataMap.putString(key, value)
            asPutDataRequest()
        }
        putDataReq.setUrgent()
        val putDataTask: Task<DataItem> = Wearable.getDataClient(this).putDataItem(putDataReq)
        putDataTask.addOnSuccessListener {
            Log.d(TAG, "Sending contact was successful: $it")
        }

        putDataTask.addOnFailureListener {
            Log.d(TAG, "Sending contact Failed: $it")
        }


    }

}
