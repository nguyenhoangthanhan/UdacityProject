package com.example.android.politicalpreparedness.network.models

import android.os.Parcelable
import com.example.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.example.android.politicalpreparedness.representative.adapter.toTypedAdapter
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Division(
        val id: String,
        val country: String,
        val state: String
) : Parcelable