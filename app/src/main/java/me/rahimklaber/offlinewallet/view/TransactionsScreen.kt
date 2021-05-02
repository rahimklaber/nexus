package me.rahimklaber.offlinewallet.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rahimklaber.offlinewallet.Transaction
import me.rahimklaber.offlinewallet.Wallet

@Composable
fun TransactionsScreen(transactions : List<Transaction>,wallet: Wallet, modifier : Modifier = Modifier){
    Card(modifier = modifier.fillMaxWidth().padding(10.dp)) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            if(transactions.isEmpty()){
                item {
                    Text("There are no transactions")
                }
            }
            items(transactions){
                Transaction(transaction = it,wallet = wallet,modifier = Modifier.padding(5.dp))
            }
        }
    }
}