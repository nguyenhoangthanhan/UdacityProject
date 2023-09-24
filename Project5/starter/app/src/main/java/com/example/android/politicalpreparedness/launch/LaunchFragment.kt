package com.example.android.politicalpreparedness.launch

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentLaunchBinding

class LaunchFragment : Fragment() {

    private val TAG = "LaunchFragment_TAG"

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentLaunchBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.btnUpcomingElections.setOnClickListener { navToElections() }
        binding.btnFindMyRepresentatives.setOnClickListener { navToRepresentatives() }

        return binding.root
    }

    private fun navToElections() {
        if (isOnline(requireContext())){
            this.findNavController().navigate(LaunchFragmentDirections.actionLaunchFragmentToElectionsFragment())
        }
        else{
            Toast.makeText(requireContext(), getString(R.string.error_internet_connection), Toast.LENGTH_LONG).show()
        }
    }

    private fun navToRepresentatives() {
        if (isOnline(requireContext())){
            this.findNavController().navigate(LaunchFragmentDirections.actionLaunchFragmentToRepresentativeFragment())
        }
        else{
            Toast.makeText(requireContext(), getString(R.string.error_internet_connection), Toast.LENGTH_LONG).show()
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_CELLULAR")
                true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_WIFI")
                true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i(TAG, "NetworkCapabilities.TRANSPORT_ETHERNET")
                true
            } else{
                false
            }
        }
        return false
    }


}
