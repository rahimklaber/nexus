package me.rahimklaber.offlinewallet.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface AssetDao{

    @Query("select * from asset where asset_name=:name and asset_name=:issuer")
    fun getByNameAndIssuer(name: String, issuer: String) : Asset?

    @Insert(onConflict = REPLACE)
    fun addAsset(asset : Asset)
}

@Dao
interface UserDao{

    @Insert(onConflict = REPLACE)
    fun addUser(user : User)

    @Query("select * from user where user.public_key == :publicKey")
    fun getByPublicKey(publicKey : String) : User?
}