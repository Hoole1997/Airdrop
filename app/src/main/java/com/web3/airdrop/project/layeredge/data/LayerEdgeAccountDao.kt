package com.web3.airdrop.project.layeredge.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.web3.airdrop.data.Wallet

@Dao
interface LayerEdgeAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(accountInfo: LayerEdgeAccountInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccountList(accountList: List<LayerEdgeAccountInfo>)

    @Query("SELECT * FROM LayerEdgeAccountInfo")
    fun getAccountList() : LiveData<List<LayerEdgeAccountInfo>>

    @Query("SELECT * FROM LayerEdgeAccountInfo WHERE walletAddress = :walletAddress")
    fun getAccountByAddress(walletAddress: String): List<LayerEdgeAccountInfo>

}