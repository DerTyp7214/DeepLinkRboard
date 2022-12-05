package de.dertyp7214.deeplinkrboard.utils

import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.core.animation.doOnEnd
import com.google.gson.Gson
import de.dertyp7214.deeplinkrboard.BuildConfig
import de.dertyp7214.deeplinkrboard.R
import de.dertyp7214.deeplinkrboard.core.content
import de.dertyp7214.deeplinkrboard.core.getTextFromUrl
import de.dertyp7214.deeplinkrboard.core.openDialog
import de.dertyp7214.deeplinkrboard.data.OutputMetadata
import java.net.URL
import androidx.core.content.edit
import de.dertyp7214.deeplinkrboard.core.preferences


class AppStartUp(private val activity: AppCompatActivity) {
    private val checkUpdateUrl by lazy {
        if (BuildConfig.DEBUG){
            "https://github.com/DerTyp7214/DeepLinkRboard/releases/download/latest-debug/output-metadata.json"
        }
        else{
            "https://github.com/DerTyp7214/DeepLinkRboard/releases/download/latest-release/output-metadata.json"
        }
    }
    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(activity) }

    private var isReady = false
    private var checkedForUpdate = false
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.splashScreen.setOnExitAnimationListener { splashScreenView ->
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.ALPHA,
                    1f,
                    0f
                )
                slideUp.interpolator = AccelerateDecelerateInterpolator()
                slideUp.duration = 200L

                slideUp.doOnEnd { splashScreenView.remove() }

                slideUp.start()
            }
        }
    }

    fun onCreate(onCreate: AppCompatActivity.(Intent) -> Unit) {
        val block: AppCompatActivity.(Intent) -> Unit = {
            isReady = true
            onCreate(this, it)
        }
        activity.apply {
            content.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isReady) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else false
                }
            })

            createNotificationChannels(this)
            checkForUpdate { update ->
                checkedForUpdate = true
                isReady = true
                validApp(this) {
                    preferences.edit { putBoolean("initialized", true) }

                    if (it) block(this, Intent().putExtra("update", update))
                    else finish()
                }}
            isReady = true
            onCreate(this, Intent())
        }
    }
    private fun checkForUpdate(callback: (update: Boolean) -> Unit) {
        if (preferences.getLong(
                "lastCheck",
                0
            ) + 5 * 60 * 100 > System.currentTimeMillis()
        ) callback(false)
        else doAsync(URL(checkUpdateUrl)::getTextFromUrl) { text ->
            try {
                val outputMetadata = Gson().fromJson(text, OutputMetadata::class.java)
                val versionCode = outputMetadata.elements.first().versionCode
                preferences.edit { putLong("lastCheck", System.currentTimeMillis()) }
                callback(versionCode > BuildConfig.VERSION_CODE)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    private fun validApp(activity: AppCompatActivity, callback: (valid: Boolean) -> Unit) {
        preferences.apply {
            var valid = getBoolean("verified", false)
            if (valid) callback(valid)
            else activity.openDialog(R.string.unreleased, R.string.notice, false, {
                it.dismiss()
                callback(valid)
            }) {
                it.dismiss()
                valid = true
                callback(valid)
                edit { putBoolean("verified", true) }
            }
        }
    }


    private fun createNotificationChannels(activity: AppCompatActivity) {
        activity.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nameDownload = getString(R.string.channel_name_download)
                val channelIdDownload = getString(R.string.download_notification_channel_id)
                val descriptionTextDownload = getString(R.string.channel_description_download)
                val importanceDownload = NotificationManager.IMPORTANCE_LOW
                val channelDownload =
                    NotificationChannel(channelIdDownload, nameDownload, importanceDownload).apply {
                        description = descriptionTextDownload
                    }

                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channelDownload)
            }
        }
    }
}