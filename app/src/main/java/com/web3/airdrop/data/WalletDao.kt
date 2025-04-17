package com.web3.airdrop.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WalletDao {

    @Insert
    fun insertWallet(wallet: Wallet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWalletList(wallet: List<Wallet>)

    @Query("SELECT * FROM Wallet WHERE chain = :chainType")
    suspend fun getWalletsByChain(chainType: String): List<Wallet>

    @Query("DELETE FROM Wallet WHERE address = :address")
    fun deleteWalletByAddress(address: String)

    @Delete
    fun deleteWallet(wallet: List<Wallet>)
}