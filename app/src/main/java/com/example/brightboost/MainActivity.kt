package com.example.brightboost

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var oldBrightness: Int = 30
    private var oldMode: Int = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
    private var receiverRegistered = false

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                restoreBrightness()
                unregisterScreenOffReceiver()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            finish()
            return
        }

        boostBrightness()
    }

    private fun boostBrightness() {
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
                IntentFilter(Intent.ACTION_SCREEN_OFF),
                Context.RECEIVER_NOT_EXPORTED
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

    override fun onDestroy() {
        unregisterScreenOffReceiver()
        super.onDestroy()
    }
}