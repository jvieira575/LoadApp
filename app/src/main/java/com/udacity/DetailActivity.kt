package com.udacity

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

/**
 * The detail [AppCompatActivity] of the application. It will display the file downloaded and the status
 * of the download. It also animates the views when doing so.
 */
class DetailActivity : AppCompatActivity() {

    companion object {

        private const val INTENT_FILE_NAME_KEY = "INTENT_FILE_NAME_KEY"
        private const val INTENT_DOWNLOAD_STATUS_KEY = "INTENT_DOWNLOAD_STATUS_KEY"

        fun newIntent(context: Context, fileName: String, downloadStatus: String): Intent {

            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(INTENT_FILE_NAME_KEY, fileName)
            intent.putExtra(INTENT_DOWNLOAD_STATUS_KEY, downloadStatus)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // Retrieve the Intent that started this activity and extract the file name and download status
        val fileName = intent.getStringExtra(INTENT_FILE_NAME_KEY) ?: "Unknown File Name"
        val downloadStatus = intent.getStringExtra(INTENT_DOWNLOAD_STATUS_KEY) ?: "Unknown Status"

        // Set the file name and download status
        file_name_text_view.text = fileName
        download_status_text_view.text = downloadStatus

        // Set the text colour to red if failed
        if (DOWNLOAD_FAILURE_STATUS == downloadStatus) {
            download_status_text_view.setTextColor(Color.RED)
        }

        // Register button listener to navigate to Main Activity
        ok_button.setOnClickListener {
            onBackPressed()
        }

        // Cancel the notifications
        val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
