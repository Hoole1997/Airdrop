package com.web3.airdrop.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.blankj.utilcode.util.Utils
import com.web3.airdrop.project.coresky.data.CoreSkyUser
import com.web3.airdrop.project.coresky.db.CoreSkyDao
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountDao
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo
import com.web3.airdrop.project.somnia.bean.SomniaAccount
import com.web3.airdrop.project.somnia.bean.SomniaDao

@Database(entities = [Wallet::class, SomniaAccount::class, LayerEdgeAccountInfo::class, CoreSkyUser::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao

    abstract fun somniaDao(): SomniaDao

    abstract fun layeredgeDao(): LayerEdgeAccountDao

    abstract fun coreSkyDao(): CoreSkyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    Utils.getApp(),
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
