package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
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

    private var  selectedDownloadUri: FILE_TO_DOWNLOAD = FILE_TO_DOWNLOAD.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //Initialize Notification Manager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Register BroadcastReceiver to track download status
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        customButton = binding.mainContent.customButton
        downloadRadioGroup = binding.mainContent.downloadRadioGroup

        customButton.setOnClickListener {


            //Operation Download
            if(selectedDownloadUri.uri.isNotEmpty()){
                //Set buttonState to Loading
                customButton.buttonState = ButtonState.Loading

                //download
                download(selectedDownloadUri.uri)
            }else{

                showToast("Please Select an Option")
            }
        }
        downloadRadioGroup.setOnCheckedChangeListener{radioGroup, i ->
            selectedDownloadUri = when(i){
                R.id.glideRadioBtn -> FILE_TO_DOWNLOAD.GLIDE
                R.id.loadAppRadioBtn -> FILE_TO_DOWNLOAD.LOAD_APP
                R.id.retrofitRadioBtn -> FILE_TO_DOWNLOAD.RETROFIT
                else -> FILE_TO_DOWNLOAD.NONE
            }
            //See selected URI in Log
            Log.i("TAGGY", selectedDownloadUri.uri)
        }

        //create notification channel
        createNotificationChannel()

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if(id == downloadID){
                val status = getDownloadStatus(id)
                if(status == DownLoadStatus.Success){
                    customButton.buttonState = ButtonState.Completed
                    showNotification(status)
                }else{
                    showToast("There was an error downloading your file")
                }

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
                DownloadManager.STATUS_SUCCESSFUL ->DownLoadStatus.Success
                DownloadManager.STATUS_FAILED -> {
                    Log.i("TAGGY", "Download Failed: Reason ${cursor.getInt(reasonIndex)}")
                    DownLoadStatus.Success
                }

                else -> {
                    Log.i("TAGGY", "Download Status:  ${cursor.getInt(statusIndex)}")
                    DownLoadStatus.Failed
                }
            }

        }
        return DownLoadStatus.None
    }

    // Function Creates Notification Channel
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "LoadAppChannel"
            val descriptionText = "Download Complete"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false) //Test this later
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    //Function creates and shows Notification
    private fun showNotification(status: DownLoadStatus){
        val detailIntent = Intent(this, DetailActivity::class.java)
        detailIntent.putExtra("fileName", selectedDownloadUri.desc)
        detailIntent.putExtra("downloadStatus",status.name)// check this
        pendingIntent = PendingIntent.getActivity(this, 0,detailIntent,PendingIntent.FLAG_UPDATE_CURRENT )

        action = NotificationCompat.Action.Builder(
            R.drawable.ic_assistant_black_24dp,
            getString(R.string.notification_button),
            pendingIntent
        ).build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.download_icon)
            .setContentTitle(selectedDownloadUri.downloadTitle)
            .setContentText(selectedDownloadUri.desc) //check
            .addAction(action)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(0,builder.build())

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(selectedDownloadUri.downloadTitle)
                .setDescription(selectedDownloadUri.desc)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {

        private enum class FILE_TO_DOWNLOAD (val uri: String, val desc: String, val downloadTitle: String){
            GLIDE (
                "https://github.com/bumptech/glide/archive/master.zip",
                "Glide - Image Loading Library By BumpTech",
                "Glide"
            ),
            LOAD_APP(
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
                "Load App - Current Repository by Udacity",
                "LoadApp"
            ),
            RETROFIT(
                "https://github.com/square/retrofit/archive/master.zip",
                "Retrofit - Type-safe HTTP client by Square, Inc",
                "Retrofit"
            ),
            NONE("","","")
        }

        private const val CHANNEL_ID = "channelId"
    }
}