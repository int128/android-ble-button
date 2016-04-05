package org.hidetake.blebutton

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("MyApplication", "Initialize preferences")
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

}
