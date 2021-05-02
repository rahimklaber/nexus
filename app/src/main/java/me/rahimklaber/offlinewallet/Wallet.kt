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
import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.moandjiezana.toml.Toml
import io.jsonwebtoken.*
import kotlinx.coroutines.*
import me.rahimklaber.offlinewallet.db.AnchorTransaction
import me.rahimklaber.offlinewallet.db.Database
import me.rahimklaber.offlinewallet.networking.AccountResolver
import org.stellar.sdk.*
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.SubmitTransactionResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PathPaymentStrictReceiveOperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional
import java.io.ByteArrayInputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * should probably hide the keypair
 * Todo: split up this class so it doesn't become a God class
 *
 */
class Wallet(val keyPair: KeyPair, val db: Database, nickname: String) : ViewModel() {

    var transactions: List<Transaction> by mutableStateOf(listOf())
    private val server = Server("https://horizon-testnet.stellar.org")
    var assetsBalances: Map<Asset.Custom, String> by mutableStateOf(mapOf())
    lateinit var account: org.stellar.sdk.Account
    val user = User(nickname, keyPair.accountId)
    val assets: List<Asset.Custom>
        get() = assetsBalances.keys.toList()
    private val jsonParser = Parser.default()


    fun getAssetByNameAndIssuer(name: String, issuer: String): Asset.Custom? {
        return assets.find {
            it.name == name && it.issuer == issuer
        }
    }

    init {
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            val sequenceNumber = server.accounts().account(keyPair.accountId).sequenceNumber
            account = org.stellar.sdk.Account(keyPair.accountId, sequenceNumber)
        }
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            updateBalance()
            try{
                server.payments().forAccount(keyPair.accountId).stream(listener)
            }catch (e  : Exception){
                println(e)
            }
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
    suspend fun getAssetsForAccount(nickname: String): List<Asset> = withContext(Dispatchers.IO){
        server
            .accounts()
            .account(resolveAddressFromNickname(nickname = nickname))
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
                        date = Date.from(Instant.ofEpochSecond((dateParser.parseBest(payment.createdAt,LocalDateTime::from,LocalDateTime::from) as LocalDateTime).toEpochSecond(
                            zoneOffset))),/*Date.from(Instant.from(dateParser.parseBest(transactionJson["started_at"] as String,LocalDateTime::from,LocalDateTime::from) as LocalDateTime))*/
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
                        date = Date.from(Instant.ofEpochSecond((dateParser.parseBest(payment.createdAt,LocalDateTime::from,LocalDateTime::from) as LocalDateTime).toEpochSecond(
                            zoneOffset))),
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
                    date = Date.from(Instant.ofEpochSecond((dateParser.parseBest(pathPayment.createdAt,LocalDateTime::from,LocalDateTime::from) as LocalDateTime).toEpochSecond(
                        zoneOffset))),
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
            }
            updateBalance()
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
        recipientNickname: String,
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
                                resolveAddressFromNickname(recipientNickname),
                                recipientAsset.toStellarSdkAsset(),
                                amount
                            )
                                .build()

                        } else {
                            /*TODO: Dynamic sendMax calculation*/
                            PathPaymentStrictReceiveOperation.Builder(
                                sendingAsset.toStellarSdkAsset(),
                                "10000000",
                                resolveAddressFromNickname(recipientNickname),
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

    suspend fun getAuthToken(asset: Asset.Custom): String {
        val jwtdb = getAuthTokenFromDb(asset = asset)
        val splitJwt: List<String>? = jwtdb?.split(".")
        println(jwtdb)
        return if (jwtdb != null && splitJwt != null) {
            val parsedJwtBody =
                jsonParser.parse(StringBuilder(splitJwt[1].decodeBase64ToString()!!)) as JsonObject
            return if ((parsedJwtBody["exp"] as Number).toLong() < Date().time) {
                println("token expired")
                getAuthTokenFromNetwork(asset = asset)
            } else {
//                println("not expired")
//                println("date expired : ${(parsedJwtBody["exp"] as Number).toLong()}")
//                println("current Date ${Date().time}")
                jwtdb
            }

        } else {
            getAuthTokenFromNetwork(asset = asset)
        }
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
    suspend fun getAuthTokenFromNetwork(asset: Asset.Custom): String = withContext(Dispatchers.IO) {
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
            Fuel.post(authServerURl)
                .jsonBody("{\"transaction\":\"${txToSign.toEnvelopeXdrBase64()}\"}")
                .response().third.get()

        val authToken =
            (parser.parse(ByteArrayInputStream(authTokenResponseBytes)) as JsonObject)["token"] as String

        db.assetDao().addAsset(
            me.rahimklaber.offlinewallet.db.Asset(
                asset.name,
                asset.issuer,
                authToken,
                asset.iconLink as String
            ).also {
                it.id = db.assetDao().getByNameAndIssuer(asset.name, asset.issuer)?.id
                    ?: 0//Todo do this better
            }
        )
        authToken
    }

    /**
     * get the response from the tx server to start the anchorTransaction session
     */
    suspend fun getInteractiveDepositSession(asset: Asset.Custom, authToken: String): JsonObject {
        val transferServerUrl = asset.toml.getString("TRANSFER_SERVER_SEP0024")
        val formData =
            listOf("asset_code" to asset.name, "account" to keyPair.accountId, "lang" to "en")

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
        return jsonParser.parse(ByteArrayInputStream(sessionData)) as JsonObject


    }

    suspend fun getInteractiveWithdrawSession(asset: Asset.Custom, authToken: String): JsonObject {
        val transferServerUrl = asset.toml.getString("TRANSFER_SERVER_SEP0024")
        val formData =
            listOf("asset_code" to asset.name, "account" to keyPair.accountId, "lang" to "en")

        val sessionData =
            withContext(Dispatchers.IO) {
                "$transferServerUrl/transactions/withdraw/interactive".httpUpload(formData)
                    .header(
                        "Authorization" to "Bearer $authToken"
                    )
                    .response().third.get()
            }
        /*
        * { type,url,id}
        * */
        return jsonParser.parse(ByteArrayInputStream(sessionData)) as JsonObject


    }

    /**
     * check on how a anchorTransaction or withdrawal is going.
     *
     * fetches the information from the anchor and updates the database.
     */
    suspend fun checkOnAnchorTransaction(
        id: String,
        dbAsset: me.rahimklaber.offlinewallet.db.Asset
    ): AnchorTransaction? {
        val asset = getAssetByNameAndIssuer(dbAsset.name, dbAsset.issuer) ?: return null
        val transferServerUrl = asset.toml.getString("TRANSFER_SERVER_SEP0024")
        return withContext(Dispatchers.IO) {
            println("$transferServerUrl/transaction")
            val authToken = getAuthToken(asset = asset)
            val response = "$transferServerUrl/transaction".httpGet(
                listOf(
                    "id" to id
                )
            )
                .header(
                    "Authorization" to "Bearer $authToken"
                ).response().third.component1() ?: return@withContext null
            val transactionJson =
                (jsonParser.parse(ByteArrayInputStream(response)) as JsonObject)["transaction"] as JsonObject
            val transaction = AnchorTransaction(
                id = id,
                kind = transactionJson["kind"] as String,
                status = transactionJson["status"] as String, /*Todo make the status user readable*/
                statusEta = (transactionJson["status_eta"] as Int?)?.toLong(),
                amountIn = transactionJson["amount_in"] as String?,
                amountOut = transactionJson["amount_out"] as String?,
                amountFee = transactionJson["amount_fee"] as String?,
                externalTransactionId = transactionJson["external_transaction_url"] as String?,
                MoreInfoUrl = transactionJson["more_info_url"] as String,
                startedAt= Date.from(Instant.ofEpochSecond((dateParser.parseBest(transactionJson["started_at"] as String,LocalDateTime::from,LocalDateTime::from) as LocalDateTime).toEpochSecond(
                    zoneOffset))),
                TransactionAssetId = dbAsset.id
            )
            db.anchorTransactionDao().addTransaction(anchorTransaction = transaction)
            transaction
        }


    }

    /**
     * send the specified amount of asset to the anchor address.
     * this used to pay the anchor to withdraw your funds.
     * This is done using a `payment` operation and not `path-payment`.
     */
    suspend fun payAnchorAsync(
        recipient: String,
        amount: String,
        memo: String,
        asset: Asset.Custom
    ): Deferred<SubmitTransactionResponse> = coroutineScope {

        val txDeferred = async(Dispatchers.Default) {
            val innterTx =
                org.stellar.sdk.Transaction.Builder(account, Network.TESTNET)
                    .addOperation(
                        PaymentOperation.Builder(recipient, asset.toStellarSdkAsset(), amount)
                            .build()
                    )
                    .setTimeout(60)
                    .setBaseFee(500)
                    .addMemo(Memo.hash(memo))
                    .build()
            innterTx.sign(keyPair)
            innterTx
        }
        async(Dispatchers.IO) {
            server.submitTransaction(txDeferred.await())
        }
    }

    /**
     * add a trustline for the specified asset.
     */
    suspend fun addAssetAsync(assetToAdd: Asset.Custom): Deferred<SubmitTransactionResponse> =
        coroutineScope {
            val txDeferred = async(Dispatchers.Default) {
                val innerTx = org.stellar.sdk.Transaction.Builder(account, Network.TESTNET)
                    .addOperation(
                        ChangeTrustOperation.Builder(
                            assetToAdd.toStellarSdkAsset(), Int.MAX_VALUE.toString()
                        ).build()
                    )
                    .setTimeout(60)
                    .setBaseFee(500)
                    .build()
                innerTx.sign(keyPair)
                innerTx
            }
            async(Dispatchers.IO) {
                server.submitTransaction(txDeferred.await())
            }

        }

    /**
     * Resolves nickname used by the wallet, to the stellar public key.
     */
    suspend fun resolveAddressFromNickname(nickname: String): String {
        val dbResolvedAddress = withContext(Dispatchers.IO){
            db.userDao().getByNickname(nickname = nickname)
        }
        return if(dbResolvedAddress != null){
            dbResolvedAddress.publicKey
        }else{
            val publicKeyResponse = AccountResolver(nickname = nickname)!! // assume it can never be null
            db.userDao().addUser(me.rahimklaber.offlinewallet.db.User(publicKeyResponse,nickname))
            publicKeyResponse
        }

    }

    /**
     * Resolves nickname used by the wallet, to the stellar public key.
     */
    suspend fun resolveNicknameFromAddress(address: String): String? {
        val dbResolvedAddress = withContext(Dispatchers.IO){
            db.userDao().getByPublicKey(address)
        }
        return if(dbResolvedAddress != null){
            dbResolvedAddress.nickName
        }else{
            val nickname = AccountResolver(nickname = address) ?: return null
            db.userDao().addUser(me.rahimklaber.offlinewallet.db.User(nickname,address))
            nickname
        }

    }
    companion object{
        val dateParser by lazy { DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'") }
        val zoneOffset by lazy { ZoneOffset.ofTotalSeconds(TimeZone.getDefault().getOffset(Date().time) /1000) }

    }


}