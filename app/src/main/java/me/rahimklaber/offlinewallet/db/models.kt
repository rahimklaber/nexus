package me.rahimklaber.offlinewallet.db

import androidx.room.*
import java.util.*
// TODO : should probably rename the classes with a `db` prefix to avoid confusion


@Entity(indices = [Index(value = ["nick_name"],unique = true)])
data class User(
    @PrimaryKey @ColumnInfo(name = "public_key") var publicKey: String,
    @ColumnInfo(name="nick_name") var nickName: String,
)

/**
 * Table for my account
 */
@Entity
data class Account(
    @PrimaryKey @ColumnInfo(name="nick_name") var nickName: String,
    @ColumnInfo(name="private_key") var privateKey : String
)

@Entity(indices = [Index(value = ["asset_name","asset_issuer"],unique = true)])
data class Asset(
    @ColumnInfo(name = "asset_name") var name: String,
    @ColumnInfo(name = "asset_issuer") var issuer: String,
    @ColumnInfo(name = "auth_token") var authToken: String?,
    @ColumnInfo(name = "icon_link") var iconLink : String? = null
){
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="asset_id") var id : Int = 0
}

@Entity(foreignKeys = [ForeignKey(entity = Asset::class,parentColumns = ["asset_id"],childColumns = ["transaction_asset_id"])])
data class AnchorTransaction(
    @PrimaryKey @ColumnInfo(name = "transaction_id") var id: String,
    @ColumnInfo(name="kind") var kind : String,
    @ColumnInfo(name = "status") var status: String? = null,
    @ColumnInfo(name = "status_eta") var statusEta: Long?= null,
    @ColumnInfo(name = "amount_in") var amountIn: String?= null,
    @ColumnInfo(name = "amount_out") var amountOut: String?= null,
    @ColumnInfo(name = "amount_fee") var amountFee: String?= null,
    @ColumnInfo(name = "started_at") var startedAt: Date?= null,
    @ColumnInfo(name = "completed_at") var completedAt: Date?= null,
    @ColumnInfo(name = "stellar_transaction_id") var stellarTransactionId: String?= null,
    @ColumnInfo(name = "external_transaction_id") var externalTransactionId: String?= null,
    @ColumnInfo(name = "external_extra") var externalExtra: String?= null,
    @ColumnInfo(name = "external_extra_text") var external_extra_text: String?= null,
/*    @ColumnInfo(name = "deposit_memo") var depositMemo: String?= null,
    @ColumnInfo(name = "deposit_memo_type") var depositMemoType: String?= null,*/
    @ColumnInfo(name = "more_info_url") var MoreInfoUrl: String?= null,
    @ColumnInfo(name = "to") var to: String?= null,
    @ColumnInfo(name = "from") var from: String?= null,
    @ColumnInfo(name="transaction_asset_id",index = true) var TransactionAssetId: Int,


)
//Todo: how to do this better
//data class AnchorTransactionWithAsset(
//    @ColumnInfo(name = "deposit_id") var depositId:  String,
//    @ColumnInfo(name = "status") var status: String,
//    @ColumnInfo(name = "status_eta") var statusEta: Long,
//    @ColumnInfo(name = "amount_in") var amountIn: String?,
//    @ColumnInfo(name = "amount_out") var amountOut: String?,
//    @ColumnInfo(name = "amount_fee") var amountFee: String?,
//    @ColumnInfo(name = "started_at") var startedAt: Date,
//    @ColumnInfo(name = "completed_at") var completedAt: Date?,
//    @ColumnInfo(name = "stellar_transaction_id") var stellarTransactionId: String?,
//    @ColumnInfo(name = "external_transaction_id") var externalTransactionId: String?,
//    @ColumnInfo(name = "external_extra") var externalExtra: String?,
//    @ColumnInfo(name = "external_extra_text") var external_extra_text: String?,
//    @ColumnInfo(name = "deposit_memo") var depositMemo: String?,
//    @ColumnInfo(name = "deposit_memo_type") var depositMemoType: String?,
//    @ColumnInfo(name = "more_info_url") var MoreInfoUrl: String?,
//    @ColumnInfo(name = "to") var to: String?,
//    @ColumnInfo(name = "from") var from: String?,
//    @ColumnInfo(name="asset_id") var assetId : Int,
//    @ColumnInfo(name = "asset_name") var name: String,
//    @ColumnInfo(name = "asset_issuer") var issuer: String,
//    @ColumnInfo(name = "auth_token") var authToken: String?,
//@ColumnInfo(name = "icon_link") var iconLink : String?
//)
data class AnchorTransactionWithAsset(
    @Embedded var anchorTransaction : AnchorTransaction,
    @Embedded var asset: Asset
)
