package com.ben.checkasenior.service

import android.util.Log
import com.ben.checkasenior.AppPreferences
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

private const val TAG = "WearDataLayerService"

class WearDataLayerListenerService : WearableListenerService() {


    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d(TAG, "onDataChanged: $dataEvents")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                if (dataMapItem.dataMap.containsKey(AppPreferences.EMERGENCY_PHONE1.first)) {
                    AppPreferences.phoneNumber1 = dataMapItem.dataMap.get(AppPreferences.EMERGENCY_PHONE1.first)
                }
                if (dataMapItem.dataMap.containsKey(AppPreferences.EMERGENCY_PHONE1_NAME.first)) {
                    AppPreferences.phoneNumber1Name = dataMapItem.dataMap.get(AppPreferences.EMERGENCY_PHONE1_NAME.first)
                }

                if (dataMapItem.dataMap.containsKey(AppPreferences.EMERGENCY_PHONE2.first)) {
                    AppPreferences.phoneNumber2 = dataMapItem.dataMap.get(AppPreferences.EMERGENCY_PHONE2.first)
                }
                if (dataMapItem.dataMap.containsKey(AppPreferences.EMERGENCY_PHONE2_NAME.first)) {
                    AppPreferences.phoneNumber2Name = dataMapItem.dataMap.get(AppPreferences.EMERGENCY_PHONE2_NAME.first)
                }

                if (dataMapItem.dataMap.containsKey(AppPreferences.EMERGENCY_PHONE3.first)) {
                    AppPreferences.phoneNumber3 = dataMapItem.dataMap.get(AppPreferences.EMERGENCY_PHONE3.first)
                }
                if (dataMapItem.dataMap.containsKey(AppPreferences.EMERGENCY_PHONE3_NAME.first)) {
                    AppPreferences.phoneNumber3Name = dataMapItem.dataMap.get(AppPreferences.EMERGENCY_PHONE3_NAME.first)
                }
                //mDataItemListAdapter.add(Event("DataItem Changed", event.dataItem.toString()))
            } else if (event.type == DataEvent.TYPE_DELETED) {
                //mDataItemListAdapter.add(Event("DataItem Deleted", event.dataItem.toString()))
            }
        }
    }

}