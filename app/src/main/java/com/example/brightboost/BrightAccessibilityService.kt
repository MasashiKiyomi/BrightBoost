package com.example.brightboost

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class BrightAccessibilityService : AccessibilityService() {

    private var oldBrightness: Int = 30
    private var oldMode: Int = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
    private var receiverRegistered = false

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                restoreBrightness()
                unregisterScreenOffReceiver()
                stopSelf()
                disableSelf()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        boostBrightness()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        restoreBrightness()
        unregisterScreenOffReceiver()
    }

    override fun onDestroy() {
        restoreBrightness()
        unregisterScreenOffReceiver()
        super.onDestroy()
    }

    private fun boostBrightness() {
        if (!Settings.System.canWrite(this)) {
            disableSelf()
            return
        }

        val resolver = contentResolver

        oldMode = Settings.System.getInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )

        oldBrightness = Settings.System.getInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS,
            30
        )

        Settings.System.putInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )

        Settings.System.putInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS,
            255
        )

        registerScreenOffReceiver()
    }

    private fun restoreBrightness() {
        val resolver = contentResolver

        Settings.System.putInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS,
            oldBrightness
        )

        Settings.System.putInt(
            resolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            oldMode
        )
    }

    private fun registerScreenOffReceiver() {
        if (!receiverRegistered) {
            registerReceiver(
                screenOffReceiver,
                IntentFilter(Intent.ACTION_SCREEN_OFF)
            )
            receiverRegistered = true
        }
    }

    private fun unregisterScreenOffReceiver() {
        if (receiverRegistered) {
            unregisterReceiver(screenOffReceiver)
            receiverRegistered = false
        }
    }
}