package com.example.android.politicalpreparedness.network.models

import androidx.room.*
import com.example.android.politicalpreparedness.election.model.ElectionModel
import com.squareup.moshi.*
import java.util.*

@Entity(tableName = "election_table")
@JsonClass(generateAdapter = true)
data class ElectionEntity(
        @PrimaryKey @Json(name="id") val id: Int,
        @ColumnInfo(name = "name") @Json(name="name") val name: String,
        @ColumnInfo(name = "electionDay") @Json(name="electionDay") val electionDay: Date,
        @Embedded(prefix = "division_") @Json(name="ocdDivisionId") val division: Division,
        @ColumnInfo(name = "isFollowing") val isFollowing: Boolean = false
)

fun ElectionEntity.toDomainModel() =
        ElectionModel(
                id = id,
                name = name,
                electionDay = electionDay,
                division = division,
                isFollowing = isFollowing
        )



fun List<ElectionEntity>.asDomainModel(): List<ElectionModel>{
        return map{
                ElectionModel(
                        id = it.id,
                        name = it.name,
                        electionDay = it.electionDay,
                        division = it.division,
                        isFollowing = it.isFollowing
                )
        }
}

fun List<ElectionModel>.asDatabaseModel(): Array<ElectionEntity>{
        return map{
                ElectionEntity(
                        id = it.id,
                        name = it.name,
                        electionDay = it.electionDay,
                        division = it.division,
                        isFollowing = it.isFollowing
                )
        }.toTypedArray()
}