package me.rahimklaber.offlinewallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Operation
import org.stellar.sdk.Server
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.TransactionResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import org.stellar.sdk.xdr.OperationType
import shadow.com.google.common.base.Optional
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import java.util.Collections.addAll

class Wallet(private val keyPair : KeyPair) : ViewModel() {

    var transactions : List<Transaction> by mutableStateOf(listOf())
    private val server = Server("https://horizon-testnet.stellar.org")
    var assetsBalances : Map<Asset,String>  by mutableStateOf(mapOf())
    init {

      var payments = runBlocking(Dispatchers.IO) {
           server.payments()
               .forAccount(keyPair.accountId)
               .execute().records
       }.toMutableList()
        payments = payments.filter { it.type == "payment" } as MutableList<OperationResponse>
        val transactionsToAdd = payments.map {
            val payment = it as PaymentOperationResponse
            if(it.sourceAccount == keyPair.accountId){
                Transaction.Sent(
                    recipient = User(payment.to),
                    asset = payment.asset.type,
                    amount =  payment.amount.toFloat(),
                    date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                    description = payment.transaction.orNull()?.memo?.toString() ?: "No desc",
                )
            }else{
                Transaction.Received(
                    from = User(payment.from),
                    asset = payment.asset.type,
                    amount =  payment.amount.toFloat(),
                    date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                    description = payment.transaction.orNull()?.memo?.toString() ?: "No desc",
                )
            }
        }

        transactions = transactions.toMutableList().also {
            it.addAll(transactionsToAdd)
        }

        GlobalScope.launch(Dispatchers.IO) {
            delay(5000)
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
                    Pair(Asset.Custom(it.assetCode, it.assetIssuer), it.balance)
                }
            }
        if (assetsBalances.isEmpty()){

            assetsResponse.map {pair ->
                assetsBalances = assetsBalances.toMutableMap().also {
                    it[pair.first] = pair.second
                }
            }
        }
        // can't set on bacground thread
//        runBlocking(Dispatchers.Main) {
//            assetsBalances.value = assetsBalances.value
//        }
        println("update balance")
    }

    fun addTransaction (tx : Transaction){
        transactions = transactions.toMutableList().also {
            it.add(tx)
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
            val tx  = when {
                response.sourceAccount == keyPair.accountId -> {
                    Transaction.Sent(
                        recipient = User(payment.to),
                        asset = payment.asset.type,
                        amount =  payment.amount.toFloat(),
                        date = /*SimpleDateFormat().parse(payment.createdAt.removeSuffix("Z")) ?:*/ Date(),
                        description = payment.transaction.orNull()?.memo?.toString() ?: "No desc",
                    )
                }
                response.to == keyPair.accountId -> {
                    Transaction.Received(
                        from = User(payment.from),
                        asset = payment.asset.type,
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
            println("error")
            print("response code ${responseCode}")
            updateBalance()
        }

    }

}