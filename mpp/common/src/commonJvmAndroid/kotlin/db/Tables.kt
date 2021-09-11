package db

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar


object AccountTable : Table<AccountModel>("account"){
    val nickname = varchar("nick_name").primaryKey().bindTo { it.nickname }
    val privateKey = varchar("private_key").bindTo { it.privateKey }
}

object UserTable : Table<UserModel>("user"){
    val publicKey = varchar("public_key").primaryKey().bindTo { it.publicKey }
    val nickname = varchar("nick_name").bindTo { it.nickname }
}
//Todo composite key
object AssetTable : Table<AssetModel>("asset"){
    var id = int("id").primaryKey().bindTo { it.id }
    var name = varchar("asset_name").bindTo { it.name }
    var issuer = varchar("asset_issuer").bindTo { it.issuer }
    var authToken = varchar("auth_token").bindTo { it.authToken }
    var icon_link = varchar("icon_Link").bindTo { it.icon_link }
}

object AnchorTransactionTable : Table<Nothing>("anchor_transaction"){
    var transactionId = varchar("transaction_id")
    var kind = varchar("kind")
    var status = varchar("status")
    var statusEta = long("status_eta")
    var amountIn = varchar("amount_in")
    var amountOut = varchar("amount_out")
    var amountFee = varchar("amount_fee")
    var startedAt = long("started_at")
    var completedAt = long("completed_at")
    var StellarTransactionId = varchar("stellar_tx_id")
    var externalTransactionId = varchar("external_tx_id")
    var externalExtra = varchar("external_extra")
    var externalExtraText = varchar("external_extra_text")
    var moreInfoUrl = varchar("more_info_url")
    var to = varchar("to")
    var from = varchar("from")
    var transactionAssetId = int("transaction_asset_id")/*.references(AssetTable,)*/
}

