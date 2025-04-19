package com.web3.airdrop.project.bless.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.web3.airdrop.project.TakerProtocol.data.TakerUser


@Dao
interface BlessNodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(info: BlessNodeInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNodeList(nodeList: List<BlessNodeInfo>)

    @Query("SELECT * FROM BlessNodeInfo WHERE address = :address")
    suspend fun getNodeByAddress(address: String): BlessNodeInfo?

    @Query("SELECT * FROM BlessNodeInfo")
    fun getNodeList(): List<BlessNodeInfo>
}