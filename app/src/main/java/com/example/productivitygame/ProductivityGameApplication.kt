package com.example.productivitygame

import android.app.Application
import com.example.productivitygame.data.AppContainer

class ProductivityGameApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}