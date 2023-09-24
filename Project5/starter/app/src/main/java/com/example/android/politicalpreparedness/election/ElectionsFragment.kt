package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {

    private lateinit var binding: FragmentElectionBinding

    // TODO: Declare ViewModel
    private val viewModel: ElectionsViewModel by lazy {
        ViewModelProvider(this, ElectionsViewModelFactory(requireActivity().application))[ElectionsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View {

        // TODO: Add binding values
        binding = FragmentElectionBinding.inflate(layoutInflater, container, false)

        // TODO: Add ViewModel values and create ViewModel
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // TODO: Link elections to voter info

        // TODO: Initiate recycler adapters

        // TODO: Populate recycler adapters

        binding.rvUpcomingElections.adapter =
            ElectionListAdapter(ElectionListener {
                viewModel.displayElectionDetails(it)
            })

        binding.rvSavedElections.adapter =
            ElectionListAdapter(ElectionListener {
                viewModel.displayElectionDetails(it)
            })
        binding.upcomingRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.navigateToSelectedProperty.observe(viewLifecycleOwner) {
            if (null != it) {
                this.findNavController().navigate(ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(it))
                viewModel.displayElectionDetailsCompleted()
            }
        }
        return binding.root
    }

    // TODO: Refresh adapters when fragment loads
}