package com.web3.airdrop.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blankj.utilcode.util.Utils
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.TakerProtocol.db.TakerProtocolDao
import com.web3.airdrop.project.bless.data.BlessNodeDao
import com.web3.airdrop.project.bless.data.BlessNodeInfo
import com.web3.airdrop.project.coresky.data.CoreSkyUser
import com.web3.airdrop.project.coresky.db.CoreSkyDao
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountDao
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo
import com.web3.airdrop.project.somnia.bean.SomniaAccount
import com.web3.airdrop.project.somnia.bean.SomniaDao
import com.web3.airdrop.project.takersowing.data.TakerSowingUser
import com.web3.airdrop.project.takersowing.db.TakerSowingDao

@Database(
    entities = [
        Wallet::class,
        SomniaAccount::class,
        LayerEdgeAccountInfo::class,
        CoreSkyUser::class,
        TakerUser::class,
        BlessNodeInfo::class,
        TakerSowingUser::class],
    version = 1,
    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 3, to = 4),
//        AutoMigration(from = 4, to = 5),
//        AutoMigration(from = 5, to = 6),
//    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao

    abstract fun somniaDao(): SomniaDao

    abstract fun layeredgeDao(): LayerEdgeAccountDao

    abstract fun coreSkyDao(): CoreSkyDao

    abstract fun takerDao(): TakerProtocolDao

    abstract fun blessNodeDao(): BlessNodeDao

    abstract fun takerSowingDao(): TakerSowingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(Utils.getApp(), AppDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}
