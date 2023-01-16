package com.blueland.kimonesheet.global

import android.app.Application
import android.content.Context

class App : Application() {

    private val TAG = javaClass.simpleName

    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun getInstance(): App {
            if (instance == null) instance = App()
            return instance!!
        }

        fun getGlobalContext(): Context? {
            return instance?.applicationContext
        }
    }
}