package com.web3.airdrop.project.TakerProtocol.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.web3.airdrop.project.TakerProtocol.data.TakerUser


@Dao
interface TakerProtocolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(accountInfo: TakerUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccountList(accountList: List<TakerUser>)

    @Query("SELECT * FROM TakerUser WHERE walletAddress = :address")
    suspend fun getAccountByAddress(address: String): TakerUser?

    @Query("SELECT * FROM TakerUser")
    fun getAccountList(): List<TakerUser>
}