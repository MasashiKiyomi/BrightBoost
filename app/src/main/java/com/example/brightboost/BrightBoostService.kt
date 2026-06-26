package com.example.brightboost

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Settings

class BrightBoostService : Service() {

    private var oldBrightness = 30
    private var oldMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
    private var boosted = false
    private var receiverRegistered = false

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                restoreBrightness()
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            1,
            createNotification()
        )

        boostBrightness()

        return START_NOT_STICKY
    }

    override fun onDestroy() {

        restoreBrightness()

        if (receiverRegistered) {
            unregisterReceiver(screenOffReceiver)
            receiverRegistered = false
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun boostBrightness() {

        if (!Settings.System.canWrite(this)) {
            stopSelf()
            return
        }

        val resolver = contentResolver

        if (!boosted) {

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

            boosted = true
        }

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

        if (!receiverRegistered) {

            registerReceiver(
                screenOffReceiver,
                IntentFilter(Intent.ACTION_SCREEN_OFF),
                Context.RECEIVER_NOT_EXPORTED
            )

            receiverRegistered = true
        }
    }

    private fun restoreBrightness() {

        if (!boosted) return

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

        boosted = false
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "brightboost",
                "BrightBoost",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        return Notification.Builder(this, "brightboost")
            .setContentTitle("BrightBoost")
            .setContentText("画面OFFで元の明るさに戻します")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }
}