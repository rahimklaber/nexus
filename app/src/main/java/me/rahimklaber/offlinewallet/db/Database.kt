package me.rahimklaber.offlinewallet.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, Asset::class],version = 1)
abstract class Database : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun userDao(): UserDao
}