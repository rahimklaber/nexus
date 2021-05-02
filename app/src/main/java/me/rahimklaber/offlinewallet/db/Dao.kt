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

    @Query("select * from user where user.nick_name = :nickname")
    fun getByNickname(nickname: String): User?

    @Query("select * from user")
    fun getAll() : List<User>
}

@Dao
interface AccountDao {

    @Insert(onConflict = REPLACE)
    fun addAccount(account: Account)

    @Query("select * from account LIMIT 1")
    fun get() : Account?
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