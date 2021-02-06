package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

/**
 * The main [AppCompatActivity] of the application. Allows the user to select one of three GIT repos to download from.
 * Once a repo is selected, and the download button is clicked, the button will animate and a download
 * will commence. A notification will appear allowing the user to click to the detail view.
 */
class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Register our receiver
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Register the click listener
        custom_button.setOnClickListener {

            // Temporary. Just testing navigation and detail activity
            val detailActivityIntent = DetailActivity.newIntent(this, INTENT_FILE_NAME_VALUE, INTENT_DOWNLOAD_STATUS_VALUE)
            startActivity(detailActivityIntent)
        }
    }

    /**
     * The [BroadcastReceiver] that is invoked when the download is complete.
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        }
    }

    /**
     * Function that initiates a download of the selected GIT repository.
     */
    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL_LOAD_APP_REPO))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    /**
     * Companion object that contains constants for use in the activity.
     */
    companion object {
        private const val TOAST_MESSAGE = "Please select the file to download"
        private const val URL_GLIDE_REPO = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_LOAD_APP_REPO = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT_REPO = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"

        // Temporary values to test detail screen
        private const val INTENT_FILE_NAME_VALUE = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val INTENT_DOWNLOAD_STATUS_VALUE = "Success"
    }
}
