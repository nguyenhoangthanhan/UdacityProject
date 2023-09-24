package com.udacity

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.udacity.custom_button.ButtonState
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = -1L
    private var downloadFileName = ""

    private var currentURL = ""

    private var downloadStatus: DownloadStatus = DownloadStatus.FAIL

    private val downloadManager by lazy {
        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }
    private var downloadContentObserver: ContentObserver? = null

    private val startForResultRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity_TAG","#MainActivity.onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        initNotification()

        addEvents()

        //request post notification. This help notifies can send on device with SDK >=33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hasNotificationPermission().not()) {
            requestNotificationPermission()
        }

    }

    private fun initNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = CHANNEL_ID
            val channelName = CHANNEL_NAME
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH).
            also {
                it.description =  getString(R.string.notification_channel_description)
                it.setShowBadge(true)
            })
        }
    }

    private fun addEvents() {
        Log.d("MainActivity_TAG","addEvents")
        binding.mainContentId.downloadOptionRadioGroup.setOnCheckedChangeListener { _, i ->
            currentURL = when(i){
                R.id.glide_rb -> GLIDE_URL
                R.id.udacity_rb -> UDACITY_URL
                R.id.retrofit_rb -> RETROFIT_URL
                else -> UDACITY_URL
            }
        }

        // TODO: Implement code below
        binding.mainContentId.loadingButton.setOnClickListener {
            Toast.makeText(this, "File is downloading", Toast.LENGTH_LONG).show()
            when (binding.mainContentId.downloadOptionRadioGroup.checkedRadioButtonId){
                View.NO_ID -> {
                    Log.d("MainActivity_TAG","addEvents.View.NO_ID")
                    Toast.makeText(this@MainActivity, "Please select the file to download", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.d("MainActivity_TAG","addEvents.else")
                    downloadFileName =
                        findViewById<RadioButton>(binding.mainContentId.downloadOptionRadioGroup.checkedRadioButtonId)
                            .text.toString()
                    download(currentURL)
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id){
                with(downloadManager.query(DownloadManager.Query().setFilterById(id))){
                    if (this != null && moveToFirst()) {
                        val statusManager = try {
                            getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        }catch (ex: java.lang.IllegalArgumentException){
                            -1
                        }

                        downloadStatus = when (statusManager) {
                            DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESS
                            DownloadManager.STATUS_FAILED -> DownloadStatus.FAIL
                            else -> DownloadStatus.UNKNOWN
                        }
                    }
                }
                unregisterDownloadContentObserver()
                if (downloadStatus != DownloadStatus.UNKNOWN){
                    createNotification()
                }
            }
        }
    }

    private fun createNotification() {
        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager

        val bundle = Bundle()
        when(currentURL){
            GLIDE_URL -> {
                bundle.putString("file_name", getString(R.string.notification_title_glide))
            }
            UDACITY_URL -> {
                bundle.putString("file_name", getString(R.string.notification_title_udacity))
            }
            RETROFIT_URL -> {
                bundle.putString("file_name", getString(R.string.notification_title_retrofit))
            }
            else -> {
                bundle.putString("file_name", getString(R.string.notification_title_udacity))
            }
        }
        bundle.putString("download_status", downloadStatus.statusText)

        val contentIntent = Intent(this, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        contentIntent.putExtras(bundle)

        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val action = NotificationCompat.Action(R.drawable.ic_assistant_black_24dp, getString(R.string.notification_button), contentPendingIntent)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentIntent(contentPendingIntent)
            .addAction(action)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                when(currentURL){
                    GLIDE_URL -> {
                        this.setContentTitle(getString(R.string.notification_title_glide))
                        this.setContentText(getString(R.string.notification_description_glide))
                    }
                    UDACITY_URL -> {
                        this.setContentTitle(getString(R.string.notification_title_udacity))
                        this.setContentText(getString(R.string.notification_description_udacity))
                    }
                    RETROFIT_URL -> {
                        this.setContentTitle(getString(R.string.notification_title_retrofit))
                        this.setContentText(getString(R.string.notification_description_retrofit))
                    }
                    else -> {
                        this.setContentTitle(getString(R.string.notification_title_udacity))
                        this.setContentText(getString(R.string.notification_description_udacity))
                    }
                }
            }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun download(URL: String) {
        Log.d("MainActivity_TAG","#download")
        if (downloadID != -1L){
            downloadManager.remove(downloadID)
            unregisterDownloadContentObserver()
            downloadID = -1L
            Log.d("MainActivity_TAG","downloadManager.remove(downloadID)")
        }

        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        createAndRegisterDownloadContentObserver()

    }

    private fun createAndRegisterDownloadContentObserver() {
        downloadContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.let {
                    queryProgress()
                }
            }
        }

        contentResolver.registerContentObserver("content://downloads/my_downloads".toUri(),
            true, downloadContentObserver!!)
    }

    private fun queryProgress() {
        downloadManager.query(DownloadManager.Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {

                    val id = try {
                        getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                    }catch (ex: java.lang.IllegalArgumentException){
                        -1
                    }

                    val statusManager = try {
                        getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    }catch (ex: java.lang.IllegalArgumentException){
                        -1
                    }

                    Log.d("MainActivity_TAG.queryProgress", "statusManager: $statusManager")
                    when (statusManager) {
                        DownloadManager.STATUS_FAILED -> {
                            Log.d("MainActivity_TAG.queryProgress","Download_queryProgress $id: failed")
                            binding.mainContentId.loadingButton.changeButtonState(ButtonState.Completed)
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            Log.d("MainActivity_TAG.queryProgress","Download_queryProgress $id: paused")
                        }
                        DownloadManager.STATUS_PENDING -> {
                            Log.d("MainActivity_TAG.queryProgress","Download_queryProgress $id: pending")
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            Log.d("MainActivity_TAG.queryProgress","Download_queryProgress $id: running")
                            binding.mainContentId.loadingButton.changeButtonState(ButtonState.Loading)
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            Log.d("MainActivity_TAG.queryProgress","Download_queryProgress $id: successful")
                            binding.mainContentId.loadingButton.changeButtonState(ButtonState.Completed)
                        }
                        -1 -> {
                            Log.d("MainActivity_TAG.queryProgress","Download_queryProgress $id: IllegalArgumentException")
                        }
                    }
                }
            }
        }
    }

    private fun unregisterDownloadContentObserver() {
        downloadContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadContentObserver = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasNotificationPermission(): Boolean {
        val notificationPermission = ContextCompat
            .checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        return notificationPermission == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() =
        startForResultRequestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unregisterDownloadContentObserver()
    }

    companion object {
        private const val CHANNEL_ID = "download_app_channelId"
        private const val CHANNEL_NAME = "download_app_channelName"

        private const val NOTIFICATION_ID = 101010

        private const val GLIDE_URL =
            "https://github.com/bumptech/glide/archive/master.zip1"

        private const val UDACITY_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"

        private const val RETROFIT_URL =
            "https://github.com/square/retrofit/archive/master.zip"

    }

    enum class DownloadStatus(val statusText: String) {
        SUCCESS("Success"), FAIL("Fail"), UNKNOWN("unknown")
    }

}