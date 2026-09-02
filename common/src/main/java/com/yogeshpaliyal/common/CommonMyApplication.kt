package com.yogeshpaliyal.common

import android.app.Application
import android.content.Intent
import com.google.android.material.color.DynamicColors

abstract class CommonMyApplication : Application() {

    abstract fun getCrashActivityIntent(throwable: Throwable): Intent

    override fun onCreate() {
        super.onCreate()

        val previewExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val intent = getCrashActivityIntent(throwable)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            previewExceptionHandler?.uncaughtException(thread, throwable)
        }

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
