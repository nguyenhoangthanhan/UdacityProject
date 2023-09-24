package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.election.model.ElectionModel
import com.example.android.politicalpreparedness.repository.ElectionsRepository
import kotlinx.coroutines.launch

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "ElectionsViewModel_TAG"

    private val database = ElectionDatabase.getInstance(application)
    private val electionsRepository = ElectionsRepository(database)

    //TODO: Create live data val for upcoming elections
    val upcomingElections: LiveData<List<ElectionModel>> = electionsRepository.allElections

    //TODO: Create live data val for saved elections
    val savedElections: LiveData<List<ElectionModel>> = Transformations.map(electionsRepository.allElections) { it ->
        it.filter { it.isFollowing }
    }

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _navigateToSelectedElectionEntity = MutableLiveData<ElectionModel?>()
    val navigateToSelectedProperty: MutableLiveData<ElectionModel?>
        get() = _navigateToSelectedElectionEntity

    init {
        viewModelScope.launch {
            Log.d("MainViewModel","init")
            electionsRepository.refreshElections()
            Log.e(TAG,"#ElectionsViewModel.upcomingElections = "+ upcomingElections.value.toString())
        }
    }


    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    fun refresh() {
        _dataLoading.value = true
        viewModelScope.launch {
            refreshElections()
            _dataLoading.value = false
        }
    }

    private suspend fun refreshElections() {
        electionsRepository.refreshElections()
    }

    //TODO: Create functions to navigate to saved or upcoming election voter info
    fun displayElectionDetails(electionModel: ElectionModel) {
        _navigateToSelectedElectionEntity.value = electionModel
    }

    fun displayElectionDetailsCompleted() {
        _navigateToSelectedElectionEntity.value = null
    }
}