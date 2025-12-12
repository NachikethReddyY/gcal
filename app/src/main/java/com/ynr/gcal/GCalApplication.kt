package com.ynr.gcal

import android.app.Application
import com.ynr.gcal.data.AppContainer

class GCalApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
