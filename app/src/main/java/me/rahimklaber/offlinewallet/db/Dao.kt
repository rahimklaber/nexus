package me.rahimklaber.offlinewallet.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AssetDao {

    @Query("select * from asset where asset_name=(:name) and asset_issuer=(:issuer)")
    fun getByNameAndIssuer(name: String, issuer: String): Asset?

    @Insert(onConflict = REPLACE)
    fun addAsset(asset: Asset)
}

@Dao
interface UserDao {

    @Insert(onConflict = REPLACE)
    fun addUser(user: User)

    @Query("select * from user where user.public_key = :publicKey")
    fun getByPublicKey(publicKey: String): User?
}

@Dao
interface DepositDao {
    @Insert(onConflict = REPLACE)
    fun addDeposit(deposit: Deposit)

    @Query("select * from deposit where deposit_id = :id")
    fun getById(id: String): Deposit?

    @Query("select * from deposit")
    fun getAll(): List<Deposit>

    @Query("select * from deposit join asset on deposit.deposit_asset_id=asset.asset_id")
    fun getDepositsWithAsset(): List<DepositWithAsset>
}