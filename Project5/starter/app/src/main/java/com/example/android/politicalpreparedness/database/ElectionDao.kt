package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.ElectionEntity

@Dao
interface ElectionDao {

    //TODO: Add insert query
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(electionEntity: ElectionEntity)

    @Query("UPDATE election_table SET isFollowing = 1 WHERE id = :id")
    suspend fun followElection(id: Int)

    @Query("UPDATE election_table SET isFollowing = 0 WHERE id = :id")
    suspend fun unFollowElection(id: Int)

    @Query("SELECT * FROM election_table WHERE isFollowing = 1")
    fun selectAllFollowedElections(): List<ElectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg electionEntities: ElectionEntity)

    //TODO: Add select all election query
    @Query("SELECT * FROM election_table ORDER BY electionDay ASC")
    fun getAllElections(): LiveData<List<ElectionEntity>>

    @Query("SELECT * FROM election_table ORDER BY electionDay ASC")
    fun getAllSync(): List<ElectionEntity>

    //TODO: Add select single election query
    @Query("SELECT * FROM election_table WHERE id = :id LIMIT 1")
    fun get(id: Int): LiveData<ElectionEntity?>

    //TODO: Add delete query

    //TODO: Add clear query
    @Query("DELETE FROM election_table")
    suspend fun deleteAll()

}