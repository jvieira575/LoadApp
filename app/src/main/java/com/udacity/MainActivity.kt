package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

/**
 * The main [AppCompatActivity] of the application. Allows the user to select one of three GIT repos to download from.
 * Once a repo is selected, and the download button is clicked, the button will animate and a download
 * will commence. A notification will appear allowing the user to click to the detail view.
 */
const val DOWNLOAD_SUCCESS_STATUS = "Success"
const val DOWNLOAD_FAILURE_STATUS = "Fail"
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

            when (radio_button_group.checkedRadioButtonId) {
                R.id.glide_radio_button -> download(URL_GLIDE_REPO)
                R.id.load_app_radio_button -> download(URL_LOAD_APP_REPO)
                R.id.retrofit_radio_button -> download(URL_RETROFIT_REPO)
                else -> Toast.makeText(this, TOAST_MESSAGE, Toast.LENGTH_SHORT).show()
            }
        }

        // Get an instance of the notification manager
        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        createNotificationChannel()
    }

    /**
     * Creates a notification channel for the application.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.description = CHANNEL_DESCRIPTION
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * The [BroadcastReceiver] that is invoked when the download is complete.
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // Ensure we have an ID
            if (id != null && downloadID == id) {

                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {

                    // Get the download status and translate it to a download status for the detail activity to display
                    val downloadStatusCode =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val downloadStatus =
                        if (downloadStatusCode == DownloadManager.STATUS_SUCCESSFUL) DOWNLOAD_SUCCESS_STATUS else DOWNLOAD_FAILURE_STATUS
                    val fileName = getSelectedDisplayFileName()

                    // Send a notification
                    notificationManager.sendNotification(fileName, downloadStatus)

                    // Safe to stop the animation
                    custom_button.setState(ButtonState.Completed)
                    custom_button.isClickable = true
                }

                // Close the cursor
                cursor.close()
            }
        }
    }

    /**
     * Retrieves the selected display file name.
     */
    private fun getSelectedDisplayFileName(): String {
        return when (radio_button_group.checkedRadioButtonId) {
            R.id.glide_radio_button -> getString(R.string.glide_radio_button_text)
            R.id.load_app_radio_button -> getString(R.string.load_app_radio_button_text)
            R.id.retrofit_radio_button -> getString(R.string.retrofit_radio_button_text)
            else -> "Unknown"
        }
    }

    /**
     * Function that initiates a download of the selected GIT repository.
     */
    private fun download(url: String) {

        // Set the state of button to loading and disable button clicks until we're finished loading
        custom_button.setState(ButtonState.Loading)
        custom_button.isClickable = false

        val request = DownloadManager.Request(Uri.parse(url))
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

        // General
        private const val TOAST_MESSAGE = "Please select the file to download"

        // Repo URLs
        private const val URL_GLIDE_REPO = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_LOAD_APP_REPO =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT_REPO =
            "https://github.com/square/retrofit/archive/master.zip"

        // Notifications
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "Load App Channel"
        private const val CHANNEL_DESCRIPTION = "The Load App Channel"
        private const val NOTIFICATION_ID = 99
    }

    /**
     * Extension function that sends notifications when a file is downloaded.
     */
    fun NotificationManager.sendNotification(
        fileName: String,
        downloadStatus: String
    ) {

        // Build the pending intent for the notification which will start the DetailActivity
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            DetailActivity.newIntent(applicationContext, fileName, downloadStatus),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the action
        action = NotificationCompat.Action.Builder(
            R.drawable.baseline_archive_white_18,
            "Check the status",
            pendingIntent
        ).build()

        // Build the actual notification
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_archive_white_18)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_description))
            )
            .setChannelId(CHANNEL_ID)
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Sends the notification
        notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Lifecycle callback. Unregister our receiver.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}