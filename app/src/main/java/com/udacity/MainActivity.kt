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
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private lateinit var customButton: LoadingButton
    private lateinit var downloadRadioGroup: RadioGroup

    private var  selectedDownloadUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Register BroadcastReceiver to track download status
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        customButton = binding.mainContent.customButton
        downloadRadioGroup = binding.mainContent.downloadRadioGroup

        customButton.setOnClickListener {


            //Operation Download
            if(selectedDownloadUri.isNotEmpty()){
                //Set buttonState to Loading
                customButton.buttonState = ButtonState.Loading

                //download
                download(selectedDownloadUri)
            }else{

                showToast("Please Select an Option")
            }
        }
        downloadRadioGroup.setOnCheckedChangeListener{radioGroup, i ->
            selectedDownloadUri = when(i){
                R.id.glideRadioBtn -> URL_GLIDE
                R.id.loadAppRadioBtn -> URL_LOAD_APP
                R.id.retrofitRadioBtn -> URL_RETROFIT
                else -> ""
            }
            //See selected URI in Log
            Log.i("TAGGY", selectedDownloadUri)
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if(id == downloadID){
                val status = getDownloadStatus(id)
                Log.i("TAGGY","Download ${status.name}")
            }
        }
    }

    private fun getDownloadStatus(id: Long?) : DownLoadStatus {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().apply {
            setFilterById(id!!)
        }
        val cursor = downloadManager.query(query)
        if(cursor.moveToFirst()){
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)

            return when (cursor.getInt(statusIndex)){
                DownloadManager.STATUS_SUCCESSFUL ->DownLoadStatus.SUCCESS
                DownloadManager.STATUS_FAILED -> {
                    Log.i("TAGGY", "Download Failed: Reason ${cursor.getInt(reasonIndex)}")
                    DownLoadStatus.FAILED
                }

                else -> {
                    Log.i("TAGGY", "Download Status:  ${cursor.getInt(statusIndex)}")
                    DownLoadStatus.FAILED
                }
            }

        }
        return DownLoadStatus.NONE
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_LOAD_APP = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zip"

        private const val CHANNEL_ID = "channelId"
    }
}