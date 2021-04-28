package me.rahimklaber.offlinewallet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [User::class, Asset::class, Deposit::class],version = 5)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun userDao(): UserDao
    abstract fun depositDao() : DepositDao
}