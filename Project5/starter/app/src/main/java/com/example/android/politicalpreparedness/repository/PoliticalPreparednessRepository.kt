package com.example.android.politicalpreparedness.repository

import android.util.Log
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class PoliticalPreparednessRepository : IPoliticalPreparednessRepository {

    override suspend fun searchRepresentatives(address: Address): List<Representative> {
        Log.d("PoliticalPreparednessRepository_searchRepresentatives:", "searchRepresentatives")
        return withContext(Dispatchers.IO){
            var listRepresentative = listOf<Representative>()
            Log.d("PoliticalPreparednessRepository_searchRepresentatives_", "Success0")
            try{
                Log.d("PoliticalPreparednessRepository_searchRepresentatives_", "Success1")
                val addressInput = address.toFormattedString()
                Log.d("PoliticalPreparednessRepository_searchRepresentatives_addressInput = ", addressInput)
                val response:RepresentativeResponse = CivicsApi.retrofitService.getRepresentatives(addressInput)
                Log.d("PoliticalPreparednessRepository_searchRepresentatives_response.offices =", response.offices.toString())
                Log.d("PoliticalPreparednessRepository_searchRepresentatives_response.officials =", response.officials.toString())
                listRepresentative = response.offices.flatMap { office -> office.getRepresentatives(response.officials) }
                Log.d("PoliticalPreparednessRepository_searchRepresentatives_", "Success2")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("PoliticalPreparednessRepository_searchRepresentatives_Failed!", e.message.toString())
            }
            listRepresentative
        }
    }
}