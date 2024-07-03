package com.example.productivitygame

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.productivitygame.data.AppContainer

class ProductivityGameApplication: Application() {
    lateinit var container: AppContainer
    lateinit var currentActivity: Activity
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityStopped(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                currentActivity = activity
            }

            override fun onActivityDestroyed(activity: Activity) {
                currentActivity = activity
            }
        })
    }
}