package me.rahimklaber.offlinewallet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [User::class, Asset::class, AnchorTransaction::class],version = 9)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun userDao(): UserDao
    abstract fun anchorTransactionDao() : AnchorTransactionDao
}