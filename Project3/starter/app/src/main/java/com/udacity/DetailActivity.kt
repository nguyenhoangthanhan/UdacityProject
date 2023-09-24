package com.udacity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val fileName = intent?.extras?.getString("file_name", "undefined") ?: "undefined"
        val downloadStatus = intent?.extras?.getString("download_status", "undefined") ?: "undefined"
        Log.d("DetailActivity_TAG", "fileName = " + fileName)
        Log.d("DetailActivity_TAG", "downloadStatus = " + downloadStatus)
        binding.idContentDetail.fileNameText.text = fileName
        binding.idContentDetail.statusText.let {
            it.text = downloadStatus
            if (downloadStatus == MainActivity.DownloadStatus.FAIL.statusText){
                it.setTextColor(getColor(R.color.red_error_color))
            }
            else{
                it.setTextColor(getColor(R.color.black))
            }
        }

        binding.idContentDetail.okButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
