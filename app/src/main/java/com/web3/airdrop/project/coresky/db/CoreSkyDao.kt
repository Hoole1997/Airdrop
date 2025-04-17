package com.web3.airdrop.project.coresky.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.web3.airdrop.project.coresky.data.CoreSkyUser

@Dao
interface CoreSkyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(accountInfo: CoreSkyUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccountList(accountList: List<CoreSkyUser>)

    @Query("SELECT * FROM CoreSkyUser WHERE address = :address")
    suspend fun getAccountByAddress(address: String): CoreSkyUser?

    @Query("SELECT * FROM CoreSkyUser")
    fun getAccountList(): List<CoreSkyUser>
}