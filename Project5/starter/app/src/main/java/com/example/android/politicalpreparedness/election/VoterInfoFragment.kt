package com.example.android.politicalpreparedness.election

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.election.model.ElectionModel
import com.example.android.politicalpreparedness.election.model.toDataModel
import com.example.android.politicalpreparedness.network.models.Address
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.*
import java.util.*

class VoterInfoFragment : Fragment() {

    private val TAG = "VoterInfoFragment_TAG"

    // TODO: Declare ViewModel
    private lateinit var viewModel: VoterInfoViewModel

    private lateinit var binding: FragmentVoterInfoBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var checkPermissionFineLocation = false
    private var checkPermissionBackgroundLocation = false

    private val startForResultRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            Log.d(TAG, "#startForResultRequestPermission")
            result.forEach {
                if (it.key == Manifest.permission.ACCESS_FINE_LOCATION && it.value) {
                    Log.d(TAG, "ACCESS_FINE_LOCATION is granted!")
                    checkPermissionFineLocation = true
                }
                else{
                    Log.d(TAG, "#startForResultRequestPermission #error - else")
                    Log.d(TAG, "it.key = " + it.key)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View {
        Log.d(TAG, "#onCreateView")

        // TODO: Add ViewModel values and create ViewModel
        binding = FragmentVoterInfoBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this, VoterInfoViewModelFactory(requireActivity().application))[VoterInfoViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.stateLocations.movementMethod = LinkMovementMethod.getInstance()
        binding.stateBallot.movementMethod = LinkMovementMethod.getInstance()

        // TODO: Add binding values
        binding = FragmentVoterInfoBinding.inflate(layoutInflater, container, false)

        // TODO: Populate voter info -- hide views without provided data.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
        */

        // TODO: Handle loading of URLs

        // TODO: Handle save button UI state
        // TODO: cont'd Handle save button clicks

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val voterInfoFragmentArgs by navArgs<VoterInfoFragmentArgs>()
        val electionDateArg = voterInfoFragmentArgs.election.electionDay.time
        val electionDate: String = ( SimpleDateFormat("E MMM dd hh:mm:ss z yyyy", Locale.US)).format(electionDateArg)

        viewModel.setCurrentElection(voterInfoFragmentArgs.election)

        binding.electionDate.text = electionDate

        getAddressFromLocation(voterInfoFragmentArgs.election)

        viewModel.electionDetails.observe(viewLifecycleOwner) {state ->
            binding.stateHeader.text = state?.name
            binding.stateLocations.setOnClickListener {
                openUrl(state?.electionAdministrationBody?.votingLocationFinderUrl)
            }
            binding.stateBallot.setOnClickListener {
                openUrl(state?.electionAdministrationBody?.ballotInfoUrl)
            }
            binding.address.text = state?.electionAdministrationBody?.correspondenceAddress?.toFormattedString()
        }

        binding.voterActionButton.setOnClickListener { viewModel.onFollowElectionClick() }

        viewModel.currentElectionModel?.observe(viewLifecycleOwner){election ->
            if (election?.isFollowing == true){
                binding.voterActionButton.text = getString(R.string.unfollow_election_text_button)
            }
            else{
                binding.voterActionButton.text = getString(R.string.follow_election_text_button)
            }
        }
    }

    private fun openUrl(url: String?){
        url?.let{
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    private fun getAddressFromLocation(election: ElectionModel) {
        Log.d(TAG, "#getAddressFromLocation")

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "#getAddressFromLocation.if")
            if (!checkPermissionFineLocation){
                startForResultRequestPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
        else{
            Log.d(TAG, "#getAddressFromLocation.else")

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
                    viewModel.loadDetails(geoCodeLocation(it), election.toDataModel())
                }

            }.addOnCompleteListener {
                Log.d(TAG, "addOnCompleteListener!")
            }.addOnFailureListener {
                Log.d(TAG, "addOnFailureListener!")
            }
        }
    }

    private fun geoCodeLocation(location: Location): Address? {
        val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
        return geocoder?.getFromLocation(location.latitude, location.longitude, 1)
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
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private suspend fun getNewAddress(location: Location): Address? {
//        return suspendCancellableCoroutine { cancellableCont ->
//            geocoder?.getFromLocation(location.latitude, location.longitude, 1) {
//                cancellableCont.resume(it.firstOrNull(), null)
//            }
//        }
//    }
}