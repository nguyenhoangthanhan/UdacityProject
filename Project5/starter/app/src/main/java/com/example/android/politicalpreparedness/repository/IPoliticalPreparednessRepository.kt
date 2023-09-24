package com.example.android.politicalpreparedness.repository

import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative

interface IPoliticalPreparednessRepository {
    suspend fun searchRepresentatives(address: Address): List<Representative>
}