package com.udacity.project4.locationreminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.databinding.ActivityRemindersBinding

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val TAG = "RemindersActivity_TAG"

    private lateinit var binding: ActivityRemindersBinding

    private val startForResultRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission())  {result ->
            if (result) {
                Log.d(TAG, "POST_NOTIFICATIONS is granted!")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //request post notification. This help notifies can send on device with SDK >=33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hasNotificationPermission().not()) {
            requestNotificationPermission()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.navHostFragment.findNavController().popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
}
