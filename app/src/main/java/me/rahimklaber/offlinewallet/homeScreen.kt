package me.rahimklaber.offlinewallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.*

val account = User("Godking")
val tx1 = Transaction.Sent(account, "USD", 20.11f, Date(), "ueet")
val tx2 = Transaction.Received(account, "USD", 20.11f, Date(), "yeet")
val transactions = (0..22).map { if (it % 2 == 0) tx1 else tx2 }
@Composable
fun HomeScreen() {
    val nav = rememberNavController()

    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

    Scaffold(
        scaffoldState = scaffoldState,
//        topBar = { TopAppBar(title = { Text("Home") }) },
        drawerContent = { },
        bottomBar = { BottomAppBar { } }
    ) {
        val padding = it
        NavHost(navController = nav,startDestination = "home"){
            composable("home"){
                Column(Modifier.padding(padding)) {
                    Balance(Modifier.padding(top = 10.dp))
                    TransactionList(transactions = transactions,modifier = Modifier.weight(1f),nav =nav)
                    BottomRow(modifier = Modifier.padding(bottom = 10.dp),nav = nav)
                }

            }
        }


    }

}

@Composable
fun Balance(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fontWeight = FontWeight(600), text = "Balance")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(painterResource(R.drawable.uscoin), null, modifier = Modifier.height(50.dp))
                Text(text = " 200.20")
            }
        }
    }
}


/**
 * Bottom row of homescreen which contains the send and receive button
 */
@Composable
fun BottomRow(modifier: Modifier = Modifier, nav : NavController) {
    //TODO why do I have to do this?
    Card(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(1f)
        ) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth(0.4f)) {
                Text(text = "Send")
            }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth(0.66f)) {
                Text(text = "Receive")
            }

        }
    }

}


/**
 * Composable representing a scrollable list of transactions.
 */
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    nav : NavController
) {

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        items(transactions) {
            Transaction(transaction = it)
        }
    }

}

/**
 * Composable representing a transaction.
 */
@Composable
fun Transaction(transaction: Transaction, onClick : ()-> Unit = {}) {
    Card(
        Modifier
            .fillMaxWidth(1f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(R.drawable.uscoin), null, modifier = Modifier.height(50.dp))
            val received = transaction is Transaction.Received
            Spacer(modifier = Modifier.width(10.dp))
            val color = if (received) Color.Green else Color.Red
            Column {
                Text(text = "${transaction.amount} ${transaction.asset}")
                val text = if (received) "received" else "sent"
                Text(fontWeight = FontWeight(10), text = text)
            }
            Spacer(modifier = Modifier.width(100.dp))
            Column {
                val text = when (transaction) {
                    is Transaction.Received -> "From ${transaction.from.name}"
                    is Transaction.Sent -> "To ${transaction.recipient.name}"
                }
                Text(text = text)
                Text(fontWeight = FontWeight(10), text = transaction.date.toString())

            }
        }
    }
}
