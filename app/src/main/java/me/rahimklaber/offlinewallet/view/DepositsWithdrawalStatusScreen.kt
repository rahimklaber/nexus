package me.rahimklaber.offlinewallet.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rahimklaber.offlinewallet.Wallet
import me.rahimklaber.offlinewallet.db.AnchorTransactionWithAsset
import me.rahimklaber.offlinewallet.ui.theme.surfaceVariant

@Composable
fun CheckDepositsWithdrawalScreen(wallet: Wallet, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf(listOf<AnchorTransactionWithAsset>()) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(wallet.assetsBalances) {
        loading = true
        transactions = withContext(Dispatchers.IO) {
            wallet.db.anchorTransactionDao().getTransactionsWithAsset()
        }
        println("loading done")
        loading = false

    }

    Card(
        modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        LazyColumn(modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (loading) {
                item {
                    CircularProgressIndicator(Modifier.padding(10.dp))
                }
            } else {
                if(transactions.isEmpty()){
                    item{
                        Text(text = "No deposits or withdrawals")
                    }
                }
                items(transactions) {
                    DepositStatus(
                        depositParam = it,
                        wallet = wallet,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }

}

@Composable
fun DepositStatus(depositParam: AnchorTransactionWithAsset, wallet: Wallet, modifier: Modifier = Modifier) {
    var deposit by remember { mutableStateOf(depositParam.anchorTransaction) }
    val asset = depositParam.asset
    var reloading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Card(
        modifier,
        backgroundColor = MaterialTheme.colors.surfaceVariant
    ) {
        Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!reloading) {


                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CoilImage(
                        imageModel = asset.iconLink ?: "xd",
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                            .padding(5.dp),
                        alignment = Alignment.Center

                    ) {
                        Text(text =asset.name)
                    }
                    Text(text = asset.name)
                }
                Spacer(modifier = Modifier.width(5.dp))
                Column {
                    Text(text = "status : ${deposit.status ?: "n/a"}")
                    Text(text = "date started: ${deposit.startedAt ?: "n/a"}")
                    Text(text = "amount deposited: ${deposit.amountIn ?: "n/a"}")
                }
                //Todo: better way to put button at the end
                Spacer(modifier = Modifier.widthIn(10.dp,100.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        reloading = true
                        deposit = wallet.checkOnAnchorTransaction(deposit.id,asset) ?: deposit
                        reloading = false
                    }

                }) {
                    Text(text = "reload")
                }
            }else{
                Row(modifier= Modifier.fillMaxWidth().widthIn(10.dp,20.dp),horizontalArrangement = Arrangement.Center,verticalAlignment = Alignment.CenterVertically){
                    CircularProgressIndicator(Modifier.padding(10.dp))
                }
            }

        }
    }

}
