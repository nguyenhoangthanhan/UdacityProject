package com.example.android.politicalpreparedness.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.election.model.ElectionModel
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.State
import com.example.android.politicalpreparedness.network.models.asDatabaseModel
import com.example.android.politicalpreparedness.network.models.asDomainModel
import com.example.android.politicalpreparedness.network.models.toDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectionsRepository(private val database: ElectionDatabase) : IElectionsRepository {

    private val TAG = "ElectionsRepository_TAG"

    override suspend fun refreshElections(): List<ElectionModel> {
        Log.e(TAG, "#getUpcomingElections")
        var listElectionEntities = listOf<ElectionModel>()
        withContext(Dispatchers.IO){
            try {
                listElectionEntities = CivicsApi.retrofitService.getElections().elections.asDomainModel()
                Log.e(TAG,"#getUpcomingElections.Success.message = "+ listElectionEntities.toString())
                val listFollowedElectionEntities = database.electionDao.selectAllFollowedElections()
                for (followedElection in listFollowedElectionEntities){
                    for(election in listElectionEntities){
                        if (followedElection.id == election.id){
                            election.isFollowing = true
                            break
                        }
                    }
                }
                database.electionDao.insertAll(*listElectionEntities.asDatabaseModel())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.e(TAG,"#getUpcomingElections.Exception.message = "+ e.message.toString())
            }
        }
        return listElectionEntities
    }

    val allElections: LiveData<List<ElectionModel>> = database.electionDao.getAllElections().map {it.asDomainModel()}

    override suspend fun setFollowElection(electionModel: ElectionModel) {
        return withContext(Dispatchers.IO){
            if (electionModel.isFollowing){
                Log.e(TAG,"#setFollowElection.isFollowing = true")
                database.electionDao.unFollowElection(electionModel.id)
            }
            else{
                Log.e(TAG,"#setFollowElection.isFollowing = false")
                database.electionDao.followElection(electionModel.id)
            }
        }
    }

    override suspend fun getElectionDetails(id: Int, address: String): State? {
        return withContext(Dispatchers.IO){
            val state = CivicsApi.retrofitService.getVoterInfo(address, id).state?.first()
            Log.e(TAG,"#getElectionDetails.state = "+ state.toString())
            state
        }
    }

    override suspend fun deleteById(id: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun getElectionFromDB(id: Int): LiveData<ElectionModel?> = database.electionDao.get(id).map { it?.toDomainModel() }

}