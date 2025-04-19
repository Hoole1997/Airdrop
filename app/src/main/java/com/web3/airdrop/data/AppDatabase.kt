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

@Database(
    entities = [
        Wallet::class,
        SomniaAccount::class,
        LayerEdgeAccountInfo::class,
        CoreSkyUser::class,
        TakerUser::class,
        BlessNodeInfo::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 3, to = 4),
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao

    abstract fun somniaDao(): SomniaDao

    abstract fun layeredgeDao(): LayerEdgeAccountDao

    abstract fun coreSkyDao(): CoreSkyDao

    abstract fun takerDao(): TakerProtocolDao

    abstract fun blessNodeDao(): BlessNodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    Utils.getApp(), AppDatabase::class.java, "app_database"
                ).addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 如果表不存在，创建表
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `TakerUser` (
                `walletAddress` TEXT NOT NULL PRIMARY KEY,
                `dcId` TEXT,
                `invitationCode` TEXT NOT NULL,
                `invitationReward` TEXT NOT NULL,
                `inviteCount` INTEGER NOT NULL,
                `lastMiningTime` INTEGER NOT NULL,
                `lastSyncTime` INTEGER NOT NULL,
                `rewardAmount` TEXT NOT NULL,
                `tgId` TEXT,
                `token` TEXT,
                `totalMiningTime` INTEGER NOT NULL,
                `totalReward` TEXT NOT NULL,
                `twId` TEXT,
                `twName` TEXT,
                `userId` INTEGER NOT NULL
            )
        """)

                // 创建索引
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_TakerUser_walletAddress` ON `TakerUser` (`walletAddress`)")
            }
        }

    }
}
