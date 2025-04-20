package com.web3.airdrop.project.takersowing.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.takersowing.data.TakerSowingUser


@Dao
interface TakerSowingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(accountInfo: TakerSowingUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccountList(accountList: List<TakerSowingUser>)

    @Query("SELECT * FROM TakerSowingUser WHERE walletAddress = :address")
    suspend fun getAccountByAddress(address: String): TakerSowingUser?

    @Query("SELECT * FROM TakerSowingUser")
    fun getAccountList(): List<TakerSowingUser>
}