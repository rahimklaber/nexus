package me.rahimklaber.offlinewallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
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
import com.skydoves.landscapist.coil.CoilImage
import org.stellar.sdk.KeyPair


val account = User("Godking")

/**
 * The maain screen of the application.
 */
val seed = "SDGJHP5WUXNDQLRLBOMIM2TSD2JZWYYLTSM5JI7LYWUIT6SQ7XMNLXXA"
val pubKey = "GBZTOCTK7UQXL2B7ABYWTLZDHCKN2YVZKBKPQJ75IL4YAYJ3OGE4FEFQ"
val wallet = Wallet(keyPair = KeyPair.fromSecretSeed(seed))

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen() {
    val nav = rememberNavController()
//    val wallet =  remember{Wallet(keyPair = KeyPair.fromSecretSeed(seed))}
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
                HomeScreen(nav = nav, wallet = wallet, modifier = Modifier.padding(padding))
            }
            composable("send") {

            }
            composable("receive") {
                Text("RECEIVE SHIT")
            }
            composable("sendByUserName"){
                SendByUserName(wallet = wallet, modifier = Modifier.padding(padding) )
            }
        }


    }

}

/**
 * Screen to select how to send money; either offline or online
 */
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
 * Includes : [Balance], [RecentTransactions] and [BottomRow]
 */
@ExperimentalMaterialApi
@Composable
fun HomeScreen(modifier: Modifier = Modifier, nav: NavController, wallet: Wallet) {

    Column(modifier = modifier) {
        Balance(Modifier.padding(10.dp), wallet.assetsBalances)
        SendOptionsRow(Modifier.padding(10.dp), nav)
        RecentTransactions(
            transactions = wallet.transactions, modifier = Modifier
                .weight(1f)
                .padding(10.dp), nav = nav
        )
        BottomRow(modifier = Modifier.padding(10.dp), nav)
    }


}

/**
 * Row showing the different options for sending assets.
 */
@Composable
fun SendOptionsRow(modifier: Modifier = Modifier, nav: NavController) {
    Card(modifier = modifier.fillMaxWidth(1f)) {


        Row(modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Send", fontWeight = FontWeight(759))
            Card(modifier.clickable {
                nav.navigate("sendByUserName")
            }){

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Online qr")
                    Icon(Icons.Default.QrCode, null, modifier.size(40.dp))
                }
            }
            Card(modifier.clickable {
                nav.navigate("sendByUserName")
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Offline qr")
                    Icon(Icons.Default.QrCode, "Offline qr", modifier.size(40.dp))

                }
            }
            Card (
                modifier.clickable {
                nav.navigate("sendByUserName")
            }){
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "username")
                    Icon(Icons.Default.Face, "Offline qr", modifier.size(40 .dp))

                }
            }
        }

    }

}

/**
 * Composable representing the transaction of the account
 *
 * @param modifier Modifier to pass.
 */
@Composable
fun Balance(modifier: Modifier = Modifier, balances: Map<Asset, String>) {

    Card(modifier = modifier.fillMaxWidth(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fontWeight = FontWeight(600), text = "Balance")

            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(balances.toList()) {( asset,assetBalance) ->
//                        Image(painterResource(R.drawable.uscoin), null, modifier = Modifier.height(50.dp))
                    Card(
                        modifier = Modifier
                            .focusModifier()
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CoilImage(
                                imageModel = asset.iconLink ?: Icons.Default.Place /*TODO this doesnt work*/,
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp),
                                alignment = Alignment.Center

                            ) {
                                Text(text = asset.name)
                            }

                            Text(text = assetBalance)
                        }

                    }

                }
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
fun BottomRow(
    modifier: Modifier = Modifier,
    nav: NavController,
) {

    val scope = rememberCoroutineScope()
    Card(modifier = modifier.fillMaxWidth(1f)) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {

            Button(
                onClick = {}, modifier = Modifier
                    .fillMaxWidth(0.4f)
            ) {
                Text(text = "Send")
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
 * @param modifier Modifier to pass.
 * @param contentPadding padding for the internal content, //TODO can this be done with the modifier?
 * @param nav NavController to use for potentially going to a "more information" page for transactions.
 *
 * TODO: can't figure out how to make top and bottom part not have a rectangular white background even though they are circular
 */
@Composable
fun RecentTransactions(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(15.dp),
    nav: NavController
) {
    val txsToUse = if (transactions.size > 4) transactions.sortedByDescending { it.date }
        .subList(0, 5) else transactions.sortedByDescending { it.date }
    Card(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Recent Transactions")
            LazyColumn(
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(6.dp),

                ) {
                items(txsToUse) {
                    Transaction(transaction = it)
                }
            }
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
            .height(50.dp)
            .fillMaxWidth(1f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoilImage(
                imageModel = transaction.asset.iconLink ?: "xd"/*just so this will fail*/,
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
            ) {
                Text(text = transaction.asset.name)
            }
            val received = transaction is Transaction.Received
            Spacer(modifier = Modifier.width(10.dp))
            val color = if (received) Color.Green else Color.Red
            Column {
                Text(text = "${transaction.amount} ${transaction.asset.name}")
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
