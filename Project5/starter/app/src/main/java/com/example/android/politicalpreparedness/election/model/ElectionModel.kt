package com.example.android.politicalpreparedness.election.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.ElectionEntity
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
data class ElectionModel(
    val id: Int,
    val name: String,
    val electionDay: Date,
    val division: Division,
    var isFollowing: Boolean = false
) : Parcelable

fun ElectionModel.toDataModel() =
        ElectionEntity(
                id = id,
                name = name,
                electionDay = electionDay,
                division = division,
                isFollowing = isFollowing
        )
