package com.udacity.project4.locationreminders.savereminder

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    private val TAG = "SaveReminderFragment_TAG"

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem

    private val startForResultRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {result ->
            var check = false
            result.forEach {
                if (it.key == ACCESS_FINE_LOCATION && it.value) {
                    Log.d(TAG, "ACCESS_FINE_LOCATION is granted!")
                    check = true
                }
                else if (it.key == ACCESS_BACKGROUND_LOCATION && it.value) {
                    Log.d(TAG, "ACCESS_BACKGROUND_LOCATION is granted!")
                    if (check){
                        checkDeviceLocationSettingsAndStartGeofence()
                    }
                }
            }
        }

    private lateinit var geoClient: GeofencingClient

    private val geofenceIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        val layoutId = R.layout.fragment_save_reminder
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geoClient = LocationServices.getGeofencingClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            val directions = SaveReminderFragmentDirections
                .actionSaveReminderFragmentToSelectLocationFragment()
            _viewModel.navigationCommand.value = NavigationCommand.To(directions)
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            // TODO: use the user entered reminder details to:
            //  1) add a geofencing request
            //  2) save the reminder to the local db

            reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
            if (hasForegroundAndBackgroundLocationPermission()){
                checkDeviceLocationSettingsAndStartGeofence()
            }
            else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForResultRequestPermission.launch(arrayOf(
                            ACCESS_FINE_LOCATION,
                            ACCESS_BACKGROUND_LOCATION
                    ))
                }
                else{
                    startForResultRequestPermission.launch(arrayOf(
                        ACCESS_FINE_LOCATION
                    ))
                }
            }

        }
    }

    private fun hasForegroundAndBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(),
                ACCESS_FINE_LOCATION)) &&
                    (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(),
                ACCESS_BACKGROUND_LOCATION))
        } else {
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(),
                ACCESS_FINE_LOCATION))
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        Log.d(TAG, "#checkDeviceLocationSettingsAndStartGeofence#start")

        // create a location request that request for the quality of service to update the location
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(100)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // check if the client location settings are satisfied
        val client = LocationServices.getSettingsClient(requireActivity())

        // create a location response that acts as a listener for the device location if enabled
        val locationResponses = client.checkLocationSettings(builder.build())

        locationResponses.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationResponses.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofence()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                addGeofence()
            } else{
                checkDeviceLocationSettingsAndStartGeofence(false)
            }

        }
    }

    private fun addGeofence() {
        Log.d(TAG, "#addGeofence#start")
        if (ActivityCompat.checkSelfPermission(
                requireContext(), ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        Log.d(TAG, "#geoClient = $geoClient")
        Log.d(TAG, "#geofenceIntent = ${geofenceIntent.toString()}")
        geoClient.addGeofences(seekGeofencing(), geofenceIntent).run {
            addOnSuccessListener {
                Log.d(TAG, "Geofence(s) added")
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
            addOnFailureListener {
                Log.e(TAG, "Failed to add geofence(s)")
            }
        }
    }

    private fun seekGeofencing(): GeofencingRequest {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude ?: -89.99,
                reminderDataItem.longitude ?: -89.99,
                100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object{
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 11
        internal const val ACTION_GEOFENCE_EVENT = "com.udacity.project4.action.ACTION_GEOFENCE_EVENT"
    }
}