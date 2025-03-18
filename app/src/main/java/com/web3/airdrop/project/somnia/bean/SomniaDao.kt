package com.web3.airdrop.project.somnia.bean

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.web3.airdrop.data.Wallet

@Dao
interface SomniaDao {

    @Query("SELECT * FROM SomniaAccount")
    fun getAllSomniaAccount(): List<SomniaAccount>

    @Insert
    fun insertSomniaAccount(somniaAccount: SomniaAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSomniaAccountList(accounts: List<SomniaAccount>)


}