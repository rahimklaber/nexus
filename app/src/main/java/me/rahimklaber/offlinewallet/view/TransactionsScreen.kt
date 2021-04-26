package me.rahimklaber.offlinewallet.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rahimklaber.offlinewallet.Transaction

@Composable
fun TransactionsScreen(transactions : List<Transaction>, modifier : Modifier = Modifier){
    Card(modifier = modifier.padding(10.dp)) {
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            items(transactions){
                Transaction(transaction = it,modifier = Modifier.padding(5.dp))
            }
        }
    }
}