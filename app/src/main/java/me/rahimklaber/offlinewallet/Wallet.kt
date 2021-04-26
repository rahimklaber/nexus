package me.rahimklaber.offlinewallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpUpload
import com.moandjiezana.toml.Toml
import kotlinx.coroutines.*
import me.rahimklaber.offlinewallet.db.Database
import org.stellar.sdk.*
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.SubmitTransactionResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PathPaymentStrictReceiveOperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional
import java.io.ByteArrayInputStream
import java.util.*

/**
 * should probably hide the keypair
 *
 */
class Wallet(val keyPair: KeyPair, val db: Database, nickname: String) : ViewModel() {

    var transactions: List<Transaction> by mutableStateOf(listOf())
    private val server = Server("https://horizon-testnet.stellar.org")
    var assetsBalances: Map<Asset, String> by mutableStateOf(mapOf())
    lateinit var account: org.stellar.sdk.Account
    val user = User(nickname, keyPair.accountId)
    val assets: List<Asset>
        get() = assetsBalances.keys.toList()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            val sequenceNumber = server.accounts().account(keyPair.accountId).sequenceNumber
            account = org.stellar.sdk.Account(keyPair.accountId, sequenceNumber)
        }
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            updateBalance()
            server.payments().forAccount(keyPair.accountId).stream(listener)
        }


    }

    /**
     * update the balances of the wallet.
     * Todo: maybe I can do this with streaming request, the same place i stream for new txs
     */
    fun updateBalance() {
        val assetsResponse = server
            .accounts()
            .account(keyPair.accountId)
            .balances.flatMap {
                if (it.assetType == "native") {
                    listOf()
                } else {
                    val (imageUrl, toml) = getAssetImageUrlandToml(it.assetCode, it.assetIssuer)
                    listOf(
                        Pair(
                            Asset.Custom(
                                it.assetCode,
                                it.assetIssuer,
                                imageUrl,
                                toml
                            ), it.balance
                        )
                    )
                }
            }
        assetsResponse.map { pair ->
            assetsBalances = assetsBalances.toMutableMap().also {
                it[pair.first] = pair.second
            }
        }

        println("update balance")
    }

    fun addTransaction(tx: Transaction) {
        transactions = transactions.toMutableList().also {
            it.add(tx)
        }
    }

    /**
     * get the asset object from the asset code and issuer
     *
     * usefull for getting the image of the asset, since this is contained in the Asset object.
     */
    fun getAsset(code: String, issuer: String): Asset? {
        return assetsBalances.keys.filter { it is Asset.Custom }.find {
            require(it is Asset.Custom)
            it.issuer == issuer && it.name == code
        }
    }

    /**
     * find all assets which an account has trustlines to.
     * Todo : should maybe put this somewhere else?
     */
    fun getAssetsForAccount(accountId: String): List<Asset> {
        return server
            .accounts()
            .account(accountId)
            .balances.flatMap {
                if (it.assetType == "native") {
                    listOf()
                } else {
                    val (imageUrl, toml) = getAssetImageUrlandToml(it.assetCode, it.assetIssuer)
                    listOf(
                        Asset.Custom(
                            it.assetCode,
                            it.assetIssuer,
                            imageUrl,
                            toml
                        )
                    )
                }
            }
    }

    /**
     * listener which handles payments.
     *
     * When a payment is received, either a normal payment or a pathpayment, the payment is added
     * to the transactionslist maintained by the wallet so it can be displayed.
     */
    private val listener = object : EventListener<OperationResponse> {

        fun handlePayment(payment: PaymentOperationResponse): Transaction? {
            val asset = when (payment.asset) {
                is AssetTypeNative -> return null
                is AssetTypeCreditAlphaNum -> getAsset(
                    (payment.asset as AssetTypeCreditAlphaNum).code,
                    (payment.asset as AssetTypeCreditAlphaNum).issuer
                )
                else -> throw Error("should never happen")
            } ?: Asset.NOT_FOUND
            return when {
                payment.sourceAccount == keyPair.accountId -> {
                    Transaction.Sent(
                        id = payment.id,
                        recipient = User(payment.to),
                        sendAsset = asset,
                        receiveAsset = asset,
                        sendAmount = payment.amount.toFloat(),
                        receiveAmount = payment.amount.toFloat(),
                        date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                        description = payment.transaction.orNull()?.memo?.toString() ?: "No desc",
                        pathPayment = false
                    )
                }
                payment.to == keyPair.accountId -> {
                    Transaction.Received(
                        id = payment.id,
                        from = User(payment.from),
                        sendAsset = asset,
                        receiveAsset = asset,
                        sendAmount = payment.amount.toFloat(),
                        receiveAmount = payment.amount.toFloat(),
                        date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                        description = payment.transaction.orNull()?.memo?.toString() ?: "No desc",
                        pathPayment = false

                    )
                }
                else -> throw Exception("Getting tx which i am not involved in")
            }
        }

        fun handlePathPaymentStrictReceive(pathPayment: PathPaymentStrictReceiveOperationResponse): Transaction? {
            val sourceAsset = Asset.fromSdkAsset(pathPayment.sourceAsset)
            val sourceIsNative = if (sourceAsset !is Asset.Native) {
                sourceAsset.iconLink = assetsBalances.keys.find { it == sourceAsset }?.iconLink
                false
            } else
                true
            val destinationAsset = Asset.fromSdkAsset(pathPayment.asset)
            val destinationIsNative = if (destinationAsset !is Asset.Native) {
                destinationAsset.iconLink =
                    assetsBalances.keys.find { it == destinationAsset }?.iconLink
                false
            } else true
            //Todo : make this nicer
            return when {
                (pathPayment.sourceAccount == keyPair.accountId && !sourceIsNative) -> Transaction.Sent(
                    id = pathPayment.id,
                    recipient = User(pathPayment.to),
                    sendAsset = sourceAsset,
                    receiveAsset = destinationAsset,
                    sendAmount = pathPayment.sourceAmount.toFloat(),
                    receiveAmount = pathPayment.amount.toFloat(),
                    date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                    description = pathPayment.transaction.orNull()?.memo?.toString()
                        ?: "No desc",
                    pathPayment = true
                )

                (pathPayment.sourceAccount != keyPair.accountId && !destinationIsNative) -> Transaction.Received(
                    id = pathPayment.id,
                    from = User(pathPayment.from),
                    sendAsset = sourceAsset,
                    receiveAsset = destinationAsset,
                    sendAmount = pathPayment.sourceAmount.toFloat(),
                    receiveAmount = pathPayment.amount.toFloat(),
                    date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                    description = pathPayment.transaction.orNull()?.memo?.toString() ?: "No desc",
                    pathPayment = true
                )
                else -> null
            }


        }

        override fun onEvent(response: OperationResponse) {
            val tx = when (response.type) {
                "payment" -> handlePayment(response as PaymentOperationResponse)
                "path_payment_strict_receive" -> handlePathPaymentStrictReceive(response as PathPaymentStrictReceiveOperationResponse)
                else -> return /*we don't support anything else.*/
            }
            if (tx != null) {
                addTransaction(tx)
                updateBalance()
            }
        }

        override fun onFailure(error: Optional<Throwable>?, responseCode: Optional<Int>?) {
            print("response code ${responseCode}")
            updateBalance()
        }

    }

    /**
     * Change this to support retries
     * Note: we assume that the asset has a toml asoociated with it
     * Returns Pair of strings with the first one being the image url and the second one
     * being the toml string
     */
    private fun getAssetImageUrlandToml(
        assetCode: String,
        assetIssuer: String
    ): Pair<String?, String?> {
        var homedomain = server
            .accounts()
            .account(assetIssuer)
            .homeDomain ?: return Pair(null, null)
        homedomain += "/.well-known/stellar.toml"
        homedomain = "https://$homedomain"
        print(homedomain)
        val (request, response, result) = homedomain.httpGet()
            .responseString()
        val (succeeded, tomlString) = when (result) {
            is com.github.kittinunf.result.Result.Failure -> {
                val ex = result.getException()
                Pair(false, "")
            }
            is com.github.kittinunf.result.Result.Success -> {
                val data = result.get()
                Pair(true, data)
            }
        }
        return if (succeeded) {
            val toml = Toml().read(tomlString)
            val string = toml.getTables("CURRENCIES").find { it.getString("code") == assetCode }
                ?.getString("image")
            Pair(string, tomlString)
        } else {
            println("failed to get toml")
            Pair(null, null)
        }

    }

    /**
     * Send an asset Either using a path payment or a normal payment, depending on the sending and receiving assets.
     *
     */
    suspend fun sendAssetAsync(
        recipientAccountId: String,
        recipientAsset: Asset,
        sendingAsset: Asset,
        amount: String,
        description: String
    ): Deferred<SubmitTransactionResponse> = coroutineScope {

        val tx =/*TODO: Dynamic sendMax calculation*/ /*TODO add config somewhere for network*/
            withContext(this.coroutineContext) {
                var txBuilder = org.stellar.sdk.Transaction.Builder(
                    account,
                    Network.TESTNET
                ) /*TODO add config somewhere for network*/
                    .addOperation(
                        if (recipientAsset == sendingAsset) {
                            PaymentOperation.Builder(
                                recipientAccountId,
                                recipientAsset.toStellarSdkAsset(),
                                amount
                            )
                                .build()

                        } else {
                            /*TODO: Dynamic sendMax calculation*/
                            PathPaymentStrictReceiveOperation.Builder(
                                sendingAsset.toStellarSdkAsset(),
                                "10000000",
                                recipientAccountId,
                                recipientAsset.toStellarSdkAsset(),
                                amount
                            )
                                .build()
                        }
                    ).setBaseFee(500)
                    .setTimeout(60)

                if (description != "") {
                    txBuilder = txBuilder.addMemo(Memo.text(description))
                }
                val tx = txBuilder.build()
                tx.sign(keyPair)
                tx
            }
        async(Dispatchers.IO) {
            server.submitTransaction(tx)
        }

    }

    suspend fun getAuthToken(asset: Asset.Custom) : String {
        return getAuthTokenFromDb(asset = asset) ?: getAuthTokenFromNetwork(asset = asset)
    }

    /**
     * get the token from the db
     */
    suspend fun getAuthTokenFromDb(asset: Asset.Custom): String? = withContext(Dispatchers.IO) {
        db.assetDao().getByNameAndIssuer(asset.name, asset.issuer)?.authToken
    }

    /**
     * get auth token from anchor
     */
    suspend fun getAuthTokenFromNetwork(asset: Asset.Custom) : String = withContext(Dispatchers.IO) {
        val parser = Parser.default()
        val authServerURl = asset.toml.getString("WEB_AUTH_ENDPOINT")

        val authResponseBytes =
            authServerURl.httpGet(
                listOf(
                    Pair("account", account.accountId)
                )
            ).response().third.component1()


        val parsedAuthResponse =
            withContext(Dispatchers.Default) {
                parser.parse(ByteArrayInputStream(authResponseBytes)) as JsonObject
            }
        val transactionXdr = parsedAuthResponse["transaction"] as String
        val networkPassphrase = parsedAuthResponse["network_passphrase"] as String
        val network = Network(networkPassphrase)
        val txToSign =
            org.stellar.sdk.Transaction.fromEnvelopeXdr(transactionXdr, network)
        txToSign.sign(keyPair)
        val authTokenResponseBytes =
            Fuel.post(authServerURl).jsonBody("{\"transaction\":\"${txToSign.toEnvelopeXdrBase64()}\"}")
                .response().third.get()

        val authToken = (parser.parse(ByteArrayInputStream(authTokenResponseBytes)) as JsonObject)["token"] as String

        db.assetDao().addAsset(me.rahimklaber.offlinewallet.db.Asset(asset.name,asset.issuer,authToken))
        authToken
    }

    /**
     * get the response from the tx server to start the deposit session
     */
    suspend fun getInteractiveDepositSession(asset: Asset.Custom, authToken : String): JsonObject{
        val parser = Parser.default()
        val transferServerUrl = asset.toml.getString("TRANSFER_SERVER_SEP0024")
        val formData = listOf("asset_code" to asset.name, "account" to keyPair.accountId, "lang" to "en")

        val sessionData =
            withContext(Dispatchers.IO) {
                "$transferServerUrl/transactions/deposit/interactive".httpUpload(formData)
                    .header(
                        "Authorization" to "Bearer $authToken"
                    )
                    .response().third.get()
            }
        /*
        * { type,url,id}
        * */
        return parser.parse(ByteArrayInputStream(sessionData)) as JsonObject



    }

}