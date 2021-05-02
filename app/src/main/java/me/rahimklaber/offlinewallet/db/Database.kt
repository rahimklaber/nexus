package me.rahimklaber.offlinewallet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [User::class, Asset::class, AnchorTransaction::class,Account::class],version = 12)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun userDao(): UserDao
    abstract fun accountDao() : AccountDao
    abstract fun anchorTransactionDao() : AnchorTransactionDao
}