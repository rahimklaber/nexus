package me.rahimklaber.offlinewallet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.util.*

val account = User("Godking")
val tx1 = Transaction.Sent(account, "USD", 20.11f, Date(), "ueet")
val tx2 = Transaction.Received(account, "USD", 20.11f, Date(), "yeet")
val transactions = (0..22).map { if (it % 2 == 0) tx1 else tx2 }

/**
 * The maain screen of the application.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen() {
    val nav = rememberNavController()

    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

    Scaffold(
        scaffoldState = scaffoldState,
//        topBar = { TopAppBar(title = { Text("Home") }) },
        drawerContent = { },
        bottomBar = { BottomAppBar { } }
    ) {
        val padding = it
        NavHost(navController = nav, startDestination = "home") {
            composable("home") {
                HomeScreen(nav = nav, modifier = Modifier.padding(padding))
            }
            composable("send") {

            }
            composable("receive") {
                Text("RECEIVE SHIT")
            }
        }


    }

}

/**
 * Screen to select how to send money; either offline or online
 */
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@Composable
fun SelectSendScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        TopAppBar {

            Text(text = "Online methods", fontWeight = FontWeight(700), fontSize = 32.sp)
        }

        Row {
            Card(modifier = Modifier.padding(10.dp)) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.qrcode),
                        contentDescription = null
                    )

                    Text(text = "Qr Code")
                }
            }


            Card(modifier = Modifier.padding(10.dp)) {
                Column {
                    Text(text = "User Name")
                }
            }


        }

        TopAppBar {

            Text(text = "Offline methods", fontWeight = FontWeight(700), fontSize = 32.sp)
        }
        Row {
            Card(modifier = Modifier.padding(10.dp)) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.qrcode),
                        contentDescription = null
                    )

                    Text(text = "Qr Code")
                }
            }


        }
    }


}


/**
 * The send screen where funds can be sent to another user.
 *
 * There are two possibilities:
 * - sending by using a name/username
 * - sending by using a qr code.
 *
 * Sending using a name is only possible when internet is available.
 *
 * Sending using a qr code is possible both when there is and there isn't internet.
 *
 */
@Composable
fun SendScreen(modifier: Modifier = Modifier) {
    var onlineTab by remember { mutableStateOf(true) }
    var offlineTab by remember { mutableStateOf(false) }
    var selectedindex by remember { mutableStateOf(0) }
    Column {
        TabRow(selectedTabIndex = selectedindex) {
            Tab(
                selected = onlineTab,
                onClick = { selectedindex = 0;onlineTab = true;offlineTab = false }) {
                Text(text = "Online")
            }
            Tab(
                selected = offlineTab,
                onClick = { selectedindex = 1;onlineTab = false;offlineTab = true }) {
                Text(text = "Offline")
            }
        }
        if (onlineTab) {
            Column(
                modifier = modifier
                    .padding(10.dp)
                    .fillMaxWidth(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var nickname by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }
                var asset by remember { mutableStateOf("") }

                Image(painter = painterResource(id = R.drawable.qrcode), contentDescription = null)
                Text(text = "recipient nickname", fontWeight = FontWeight(50))
                TextField(value = nickname, onValueChange = { nickname = it })
                Text(text = "Asset", fontWeight = FontWeight(50))
                TextField(value = asset, onValueChange = { asset = it })
                Text(text = "amount", fontWeight = FontWeight(50))
                TextField(value = amount, onValueChange = { amount = it })
                Text(text = "description", fontWeight = FontWeight(50))
                TextField(value = description, onValueChange = { description = it })
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                Button(modifier = Modifier.fillMaxWidth(0.3f), onClick = {}) {
                    Text(text = "SEND")
                }
            }
        } else if (offlineTab) {
            Text(text = "Offline")
        }

    }


}

/**
 * Composable representing the Homescreen of the app.
 * Includes : [Balance], [TransactionList] and [BottomRow]
 */
@ExperimentalMaterialApi
@Composable
fun HomeScreen(modifier: Modifier = Modifier, nav: NavController) {
    Column(modifier = modifier) {
        Balance(Modifier.padding(top = 10.dp))
        TransactionList(transactions = transactions, modifier = Modifier.weight(1f), nav = nav)
        BottomRow(modifier = Modifier.padding(bottom = 10.dp), nav = nav)
    }
}

/**
 * Composable representing the transaction of the account
 *
 * @param modifier Modifier to pass.
 */
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
 * @param modifier modifier to pass in
 * @param nav the nav controller to use to get to the send and receive page.
 */
@ExperimentalMaterialApi
@Composable
fun BottomRow(modifier: Modifier = Modifier, nav: NavController) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    val coroutineScope = rememberCoroutineScope()

    Card(modifier = modifier.height(100.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            BottomSheetScaffold(
                scaffoldState = bottomSheetScaffoldState,
                sheetContent = { SelectSendScreen() },
                sheetPeekHeight = 0.dp,
                modifier = Modifier.fillMaxWidth(0.4f)
            ) {
                Button(onClick = {
                    coroutineScope.launch {

                        if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        } else {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    }
                }, modifier = Modifier
                    .padding(it)
                    .fillMaxWidth(0.4f)) {
                    Text(text = "Send")
                }
            }


            Button(onClick = { nav.navigate("receive") }, modifier = Modifier.fillMaxWidth(0.66f)) {
                Text(text = "Receive")
            }

        }
    }

}


/**
 * Composable representing a scrollable list of transactions.
 *
 * Uses a lazyColumn under the hood.
 *
 * @param transactions list of transactions to render.
 * @param modifier Modifier to pass.
 * @param contentPadding padding for the internal content, //TODO can this be done with the modifier?
 * @param nav NavController to use for potentially going to a "more information" page for transactions.
 *
 * TODO: can't figure out how to make top and bottom part not have a rectangular white background even though they are circular
 */
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(15.dp),
    nav: NavController
) {

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        items(transactions) {
            Transaction(transaction = it)
        }
    }

}

/**
 * Composable representing a transaction.
 *
 * @param transaction transaction to render
 * @param onClick onclick handler, mostlikely for when there is a more info page for a transaction
 */
@Composable
fun Transaction(transaction: Transaction, onClick: () -> Unit = {}) {
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
