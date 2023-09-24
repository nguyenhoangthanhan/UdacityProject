package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.election.model.ElectionModel
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.ElectionEntity
import com.example.android.politicalpreparedness.network.models.State
import kotlinx.coroutines.launch
import com.example.android.politicalpreparedness.repository.ElectionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VoterInfoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "VoterInfoViewModel_TAG"

    private val database = ElectionDatabase.getInstance(application)
    private val electionsRepository = ElectionsRepository(database)

    //TODO: Add live data to hold voter info
    var currentElectionModel: LiveData<ElectionModel?>? = null

    private val _electionDetails: MutableLiveData<State?> = MutableLiveData()
    val electionDetails: LiveData<State?>
        get() = _electionDetails

    //TODO: Add var and methods to populate voter info

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */
    init {
    }

    fun setCurrentElection(electionModel: ElectionModel){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                currentElectionModel = electionsRepository.getElectionFromDB(electionModel.id)
            }
        }
    }

    fun loadDetails(address: Address?, electionEntity: ElectionEntity) {
        viewModelScope.launch {
            if (address != null){
                Log.d(TAG, "loadDetails!. Address = " + address.toFormattedString())
                val response = electionsRepository.getElectionDetails(electionEntity.id, address.toFormattedString())
                _electionDetails.value = response
                Log.d(TAG,"#loadDetails.state = "+ _electionDetails.value.toString())
            }
            else{
                Log.d(TAG, "loadDetails.fail!. Address = null")
            }
        }
    }

    public fun onFollowElectionClick() {
        viewModelScope.launch {
            Log.d(TAG, "#onFollowElectionClick")
            currentElectionModel?.value?.let { electionsRepository.setFollowElection(it) }
        }
    }

}