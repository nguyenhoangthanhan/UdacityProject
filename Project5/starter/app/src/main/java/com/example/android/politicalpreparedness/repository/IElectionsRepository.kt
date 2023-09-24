package com.example.android.politicalpreparedness.repository

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.election.model.ElectionModel
import com.example.android.politicalpreparedness.network.models.ElectionEntity
import com.example.android.politicalpreparedness.network.models.State

interface IElectionsRepository {
    suspend fun refreshElections(): List<ElectionModel>
    suspend fun setFollowElection(electionModel: ElectionModel)
    suspend fun getElectionDetails(id: Int, address: String): State?
    suspend fun deleteById(id: Int)
    suspend fun getElectionFromDB(id: Int): LiveData<ElectionModel?>
}