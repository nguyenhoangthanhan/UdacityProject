package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DetailFragment : Fragment() {

    private val TAG = "DetailFragment_TAG"

    companion object {
        //TODO: Add Constant for Location request
    }

    private lateinit var binding: FragmentRepresentativeBinding

    //TODO: Declare ViewModel
    val viewModel: RepresentativeViewModel by viewModel()

    lateinit var fusedLocationClient: FusedLocationProviderClient

    private var checkPermissionFineLocation = false

    private val startForResultRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {result ->
            Log.d(TAG, "#startForResultRequestPermission")
            result.forEach {
                if (it.key == Manifest.permission.ACCESS_FINE_LOCATION && it.value) {
                    Log.d(TAG, "ACCESS_FINE_LOCATION is granted!")
                    checkPermissionFineLocation = true
                    getLocation()
                }
                else{
                    Log.d(TAG, "#startForResultRequestPermission #error - else")
                    Log.d(TAG, "it.key = " + it.key)
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //TODO: Establish bindings
        binding = FragmentRepresentativeBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.representativeContainer.setTransition(R.id.start, R.id.start)

        //TODO: Define and assign Representative adapter

        //TODO: Populate Representative adapter

        //TODO: Establish button listeners for field and location search

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.representativeRecycler.adapter = RepresentativeListAdapter()

        viewModel.representatives2.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                Log.d(TAG, "#viewModel.representatives2.observe.it.isNullOrEmpty() .  View.VISIBLE")
                binding.tvShowNoRepresentative.visibility = View.VISIBLE
            }
            else{
                Log.d(TAG, "#viewModel.representativ2.observe .  View.GONE")
                binding.tvShowNoRepresentative.visibility = View.GONE
            }
            Log.d(TAG, "#viewModel.representatives2.observe")
            val adapter = binding.representativeRecycler.adapter as RepresentativeListAdapter
            adapter.submitList(it)
        }
        viewModel.message.observe(viewLifecycleOwner) {
            it?.let {
                showSnackBar(getString(it))
            }
        }

        viewModel.messageString.observe(viewLifecycleOwner) {
            it?.let {
                showSnackBar(it)
            }
        }
        viewModel.dataLoading.observe(viewLifecycleOwner) {
            if (it) {
                hideKeyboard()
            }
        }

        binding.state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.setState(requireContext().resources.getStringArray(R.array.states)[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.buttonLocation.setOnClickListener {

            if (!checkLocationPermissions()){
                Toast.makeText(requireContext(), getString(R.string.permission_denied_explanation), Toast.LENGTH_LONG).show()
            }
            else{
                getLocation()
            }
        }

        binding.buttonSearch.setOnClickListener {
            Log.d(TAG, "#binding.buttonSearch.click")

            hideKeyboard()
            val address = Address(binding.addressLine1.text.toString(),
                binding.addressLine2.text.toString(),
                binding.city.text.toString(),
                binding.state.selectedItem.toString(),
                binding.zip.text.toString())

            Log.d(TAG, "#binding.buttonSearch.click.address.toFormattedString() = ")

            viewModel.searchForRepresentatives(address)
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
    }

    private fun checkLocationPermissions(): Boolean {
        Log.d(TAG, "#checkLocationPermissions")
        return if (isPermissionGranted()) {
            Log.d(TAG, "#checkLocationPermissions - isPermissionGranted() = true")
            true
        } else {
            if (!checkPermissionFineLocation){
                startForResultRequestPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
            return checkPermissionFineLocation
        }
    }

    private fun isPermissionGranted() : Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getLocation() {
        //TODO: Get location from LocationServices
        //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
//        binding.representativesLoading.fadeIn()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            return
        }
        fusedLocationClient.getCurrentLocation(100, object : CancellationToken() {
            override fun isCancellationRequested(): Boolean {
                return false
            }

            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                return this
            }

        }).addOnSuccessListener {
            Log.d(TAG, "addOnSuccessListener!")
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.searchForRepresentatives(geoCodeLocation(it))
            }

        }.addOnCompleteListener {
            Log.d(TAG, "addOnCompleteListener!")
        }.addOnFailureListener {
            Log.d(TAG, "addOnFailureListener!")
        }
    }

    private fun geoCodeLocation(location: Location): Address? {
        var result: Address? = null
        val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
        try {
            result = geocoder?.getFromLocation(location.latitude, location.longitude, 1)
                ?.map { address ->
                    Address(
                        address.thoroughfare.orEmpty(),
                        address.subThoroughfare.orEmpty(),
                        address.locality.orEmpty(),
                        address.adminArea.orEmpty(),
                        address.postalCode.orEmpty()
                    )
                }
                ?.first()
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
        return result

    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}