package me.rahimklaber.offlinewallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.kittinunf.fuel.httpGet
import com.moandjiezana.toml.Toml
import kotlinx.coroutines.*
import org.stellar.sdk.*
import org.stellar.sdk.Account
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.SubmitTransactionResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import shadow.com.google.common.base.Optional
import java.util.*

class Wallet(private val keyPair : KeyPair) : ViewModel() {

    var transactions : List<Transaction> by mutableStateOf(listOf())
    private val server = Server("https://horizon-testnet.stellar.org")
    var assetsBalances : Map<Asset,String>  by mutableStateOf(mapOf())
    lateinit var account : Account
    init {
        GlobalScope.launch(Dispatchers.IO) {
            delay(1000)
            val sequenceNumber = server.accounts().account(keyPair.accountId).sequenceNumber
            account = org.stellar.sdk.Account(keyPair.accountId,sequenceNumber)
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
    fun updateBalance(){
        val assetsResponse = server
            .accounts()
            .account(keyPair.accountId)
            .balances.map {
                if (it.assetType=="native"){
                    Pair(Asset.Native,it.balance)
                }else {
                    Pair(Asset.Custom(it.assetCode, it.assetIssuer,getAssetUrl(it.assetCode,it.assetIssuer)), it.balance)
                }
            }
            assetsResponse.map {pair ->
                assetsBalances = assetsBalances.toMutableMap().also {
                    it[pair.first] = pair.second
                }
            }

        println("update balance")
    }

    fun addTransaction (tx : Transaction){
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
        return assetsBalances.keys.filter { it is Asset.Custom}.find {
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
            .balances.map {
                if (it.assetType == "native") {
                    Asset.Native
                } else {

                        Asset.Custom(
                            it.assetCode,
                            it.assetIssuer,
                            getAssetUrl(it.assetCode, it.assetIssuer)
                        )
                }
            }
    }

    private val listener = object : EventListener<OperationResponse>{
        override fun onEvent(response: OperationResponse) {
            if (response.type != "payment")
                return
//            println("response is payment")
            val payment = response as PaymentOperationResponse
            println(payment.from == keyPair.accountId)
            if(payment.to != keyPair.accountId && payment.from != keyPair.accountId)
                return
            println("I am recipient")
            val asset = when(payment.asset){
                is AssetTypeNative -> Asset.Native
                is AssetTypeCreditAlphaNum -> getAsset((payment.asset as AssetTypeCreditAlphaNum).code,
                    (payment.asset as AssetTypeCreditAlphaNum).issuer)
                else -> throw Error("should never happen")
            } ?: Asset.NOT_FOUND
            val tx  = when {
                response.sourceAccount == keyPair.accountId -> {
                    Transaction.Sent(
                        recipient = User(payment.to),
                        asset = asset  ,
                        amount =  payment.amount.toFloat(),
                        date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                        description = payment.transaction.orNull()?.memo?.toString() ?: "No desc",
                    )
                }
                response.to == keyPair.accountId -> {
                    Transaction.Received(
                        from = User(payment.from),
                        asset = asset ?: Asset.Native,
                        amount =  payment.amount.toFloat(),
                        date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                        description = payment.transaction.orNull()?.memo?.toString() ?: "No desc",
                    )
                }
                else -> throw Exception("Getting tx which i am not involved in")
            }

            addTransaction(tx)
            updateBalance()
        }

        override fun onFailure(error: Optional<Throwable>?, responseCode: Optional<Int>?) {
            print("response code ${responseCode}")
            updateBalance()
        }

    }
    fun getAssetUrl(assetCode : String, assetIssuer: String): String?{
        var homedomain = server
            .accounts()
            .account(assetIssuer)
            .homeDomain ?: return null
        homedomain+="/.well-known/stellar.toml"
        homedomain= "https://$homedomain"
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
        return if(succeeded){
            val toml = Toml().read(tomlString)
            val string = toml.getTables("CURRENCIES").find { it.getString("code") == assetCode }
                ?.getString("image")
            string
        }else{
         null
        }

    }

    /**
     * Send an asset Either using a path payment or a normal payment, depending on the sending and receiving assets.
     *
     */
    suspend fun sendAsset(recipientAccountId : String, recipientAsset: Asset, sendingAsset: Asset, amount: String, description: String): Deferred<SubmitTransactionResponse> = coroutineScope{

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
                    ).setBaseFee(200)
                    .setTimeout(25)

                if (description != "") {
                    txBuilder = txBuilder.addMemo(Memo.text(description))
                }
                val tx = txBuilder.build()
                tx.sign(keyPair)
                tx
            }
        async(Dispatchers.IO){
            server.submitTransaction(tx)
        }

    }

}