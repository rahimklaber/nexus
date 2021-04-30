package me.rahimklaber.offlinewallet.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

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

/**
 * Table for withdrawals and deposits at an anchor
 */
@Dao
interface AnchorTransactionDao {
    @Insert(onConflict = REPLACE)
    fun addTransaction(anchorTransaction: AnchorTransaction)

    @Query("select * from anchorTransaction where transaction_id = :id")
    fun getById(id: String): AnchorTransaction?

    @Query("select * from anchorTransaction")
    fun getAll(): List<AnchorTransaction>

    @Query("select * from anchorTransaction join asset on anchorTransaction.transaction_asset_id=asset.asset_id")
    fun getTransactionsWithAsset(): List<AnchorTransactionWithAsset>
}