package me.rahimklaber.offlinewallet.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey @ColumnInfo(name="public_key") val publicKey : String,
)

@Entity(primaryKeys = ["asset_name","asset_issuer"])
data class Asset(
//    @PrimaryKey val uid : Int,
    @ColumnInfo(name="asset_name") val name : String,
    @ColumnInfo(name="asset_issuer") val issuer : String,
    @ColumnInfo(name="auth_token") val authToken : String?
)