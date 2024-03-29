package me.rahimklaber.offlinewallet.view

import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rahimklaber.offlinewallet.*
import me.rahimklaber.offlinewallet.R
import me.rahimklaber.offlinewallet.ui.theme.surfaceVariant
import org.stellar.sdk.KeyPair


val account = User("Godking")

/**
 * The maain screen of the application.
 */


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(wallet : Wallet) {
    val nav = rememberNavController()
//    val wallet =  remember{Wallet(keyPair = KeyPair.fromSecretSeed(seed))}
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    var title by remember { mutableStateOf("Home") }
    val scope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = false,
        topBar = {
            TopAppBar(title = { Text(title) }, navigationIcon = {
                IconButton(onClick = { scope.launch { if (scaffoldState.drawerState.isOpen) scaffoldState.drawerState.close() else scaffoldState.drawerState.open() } }) {
                    Icon(Icons.Default.Menu, null)
                }
            })
        },
        drawerContent = {
            Spacer(Modifier.height(20.dp))
            Text(modifier=Modifier.padding(5.dp),fontWeight = FontWeight(600), text = "Welcome ${wallet.user.name}", fontSize = 20.sp,textDecoration = TextDecoration.Underline)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { nav.navigate("home");scope.launch { if (scaffoldState.drawerState.isOpen) scaffoldState.drawerState.close() else scaffoldState.drawerState.open() } },
                Modifier.padding(5.dp)
            ) {
                Text("Home")
            }
            Button(
                onClick = { nav.navigate("assets");scope.launch { if (scaffoldState.drawerState.isOpen) scaffoldState.drawerState.close() else scaffoldState.drawerState.open() } },
                Modifier.padding(5.dp)
            ) {
                Text("Assets")
            }
            Button(
                onClick = { nav.navigate("transactions");scope.launch { if (scaffoldState.drawerState.isOpen) scaffoldState.drawerState.close() else scaffoldState.drawerState.open() } },
                Modifier.padding(5.dp)
            ) {
                Text("Transactions")
            }
            Button(
                onClick = { nav.navigate("depositOrWithdraw");scope.launch { if (scaffoldState.drawerState.isOpen) scaffoldState.drawerState.close() else scaffoldState.drawerState.open() } },
                Modifier.padding(5.dp)
            ) {
                Text("Deposit / Withdraw")
            }
            Button(
                onClick = { nav.navigate("depositsOrWithdrawalStatus");scope.launch { if (scaffoldState.drawerState.isOpen) scaffoldState.drawerState.close() else scaffoldState.drawerState.open() } },
                Modifier.padding(5.dp)
            ) {
                Text("Deposits / withdrawals status")
            }


        }
    ) {
        val padding = it
        NavHost(navController = nav, startDestination = "home") {
            composable("home") {
                title = "home"
                HomeScreen(nav = nav, wallet = wallet, modifier = Modifier.padding(padding))
            }
            composable("send") {

            }
            composable("receive") {
                Text("RECEIVE SHIT")
            }
            composable("sendByUserName") {
                title = "Send to User"
                SendByUserName(wallet = wallet, modifier = Modifier.padding(padding))
            }
            composable("transactions") {
                title = "Transactions"
                TransactionsScreen(transactions = wallet.transactions,wallet = wallet)
            }
            composable("depositOrWithdraw") {
                title = "deposit or Withdraw"
                DepositOrWithdrawScreen(wallet = wallet)
            }
            composable("depositsOrWithdrawalStatus") {
                title = "Check Deposits/Withdrawals status"
                CheckDepositsWithdrawalScreen(wallet = wallet)
            }
            composable("assets") {
                title = "Assets"
                AssetsScreen(wallet = wallet)
            }
            composable("createqr"){
                title = "Create Payment Request"
                CreateQrForReceiveScreen(wallet = wallet)
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

    LazyColumn(modifier = modifier) {

        item {
            Balance(Modifier.padding(10.dp), wallet.assetsBalances)

        }
        item {
            SendOptionsRow(Modifier.padding(10.dp), nav)
        }
//        item {
//            ReceiveOptionsRow(modifier = Modifier.padding(10.dp),nav)
//        }

        item {
            RecentTransactions(
                transactions = wallet.transactions,wallet, modifier = Modifier
                    /*.weight(1f)*/
                    .padding(10.dp), nav = nav
            )
        }

//        BottomRow(modifier = Modifier.padding(10.dp), nav)
    }


}

/**
 * Row showing the different options for sending assets.
 */
@Composable
fun SendOptionsRow(modifier: Modifier = Modifier, nav: NavController) {
    Card(modifier = modifier.fillMaxWidth(1f)) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Send", fontWeight = FontWeight(759),fontSize = 20.sp)
            Row(modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween) {
//                Card(
//                    modifier.clickable {
//                        nav.navigate("sendByUserName")
//                    },
//                    backgroundColor = MaterialTheme.colors.surfaceVariant
//                ) {
//
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text(text = "QR code")
//                        Icon(Icons.Default.QrCode, null, modifier.size(40.dp))
//                    }
//                }
//                Card(
//                    modifier.clickable {
//                        nav.navigate("sendByUserName")
//                    },
//                    backgroundColor = MaterialTheme.colors.surfaceVariant
//                ) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text(text = "Offline qr")
//                        Icon(Icons.Default.QrCode, "Offline qr", modifier.size(40.dp))
//
//                    }
//                }
                Card(
                    modifier.clickable {
                        nav.navigate("sendByUserName")
                    },
                    backgroundColor = MaterialTheme.colors.surfaceVariant
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "username")
                        Icon(Icons.Default.Face, "Offline qr", modifier.size(40.dp))

                    }
                }
            }

        }
    }

}
@Composable
fun ReceiveOptionsRow(modifier: Modifier = Modifier, nav: NavController) {
    Card(modifier = modifier.fillMaxWidth(1f)) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Receive", fontWeight = FontWeight(759),fontSize = 20.sp)
            Row(modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween) {
                Card(
                    modifier.clickable {
                        nav.navigate("createqr")
                    },
                    backgroundColor = MaterialTheme.colors.surfaceVariant
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "QR code")
                        Icon(Icons.Default.QrCode, null, modifier.size(40.dp))
                    }
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
fun Balance(modifier: Modifier = Modifier, balances: Map<Asset.Custom, String>) {

    Card(modifier = modifier.fillMaxWidth(1f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fontWeight = FontWeight(600), text = "Balance",fontSize = 20.sp)

            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if(balances.isEmpty()){
                    item { 
                        Text("You have no assets")
                    }
                }
                items(balances.toList()) { (asset, assetBalance) ->
//                        Image(painterResource(R.drawable.uscoin), null, modifier = Modifier.height(50.dp))
                    Card(
                        modifier = Modifier
                            .focusModifier()
                            .padding(10.dp),
                        backgroundColor = MaterialTheme.colors.surfaceVariant
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CoilImage(
                                imageModel = asset.iconLink
                                    ?: Icons.Default.Place /*TODO this doesnt work*/,
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp),
                                alignment = Alignment.Center

                            ) {
                                Text(text = asset.name)
                            }
                            val split = assetBalance.split(".")
                            val beforeDecimal = split[0]
                            val afterDecimal = if (split[1].length > 1) split[1].substring(0,2) else split[1]
                            Text(text = "$beforeDecimal.$afterDecimal ${asset.name}",Modifier.padding(5.dp))
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
    wallet: Wallet,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(15.dp),
    nav: NavController
) {
    val txsToUse = if (transactions.size > 4) transactions.sortedByDescending { it.date }
        .subList(0, 5) else transactions.sortedByDescending { it.date }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Recent Transactions",fontSize = 20.sp,fontWeight = FontWeight(600) )
            if(txsToUse.isEmpty()){
                Text(text = "there are no transactions")
            }
            for (tx in txsToUse) {
                Transaction(transaction = tx,wallet = wallet, modifier = Modifier.padding(5.dp))
            }
        }
    }
}


/**
 * Composable representing a transaction.
 *
 * @param transaction transaction to render
 * @param onClick onclick handler, most likely for when there is a more info page for a transaction
 */
@Composable
fun Transaction(transaction: Transaction, wallet: Wallet, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {

    val scope = rememberCoroutineScope()
    var recipientOrSender by remember{ mutableStateOf("loading")}
    var loading by remember{ mutableStateOf(true)}
    val context = LocalContext.current
    LaunchedEffect(key1 = true){
        if(loading){
            launch(Dispatchers.IO) {
                val toResolve = when (transaction) {
                    is Transaction.Received -> transaction.from.name
                    is Transaction.Sent -> transaction.recipient.name
                }
                recipientOrSender = wallet.resolveNicknameFromAddress(toResolve) ?: "External Account"
                loading = false
            }
        }
    }
    Card(
        modifier
//            .height(50.dp)
            .fillMaxWidth(1f),
        backgroundColor = MaterialTheme.colors.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val assetToShow = when (transaction) {
                is Transaction.Received -> transaction.receiveAsset
                is Transaction.Sent -> transaction.sendAsset
            }
            CoilImage(
                imageModel = assetToShow.iconLink ?: "xd"/*just so this will fail*/,
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)
            ) {
                Text(text = assetToShow.name)
            }
            val received = transaction is Transaction.Received
            Spacer(modifier = Modifier.width(10.dp))
            val amountSentOrReceived = when (transaction) {
                is Transaction.Sent -> transaction.sendAmount
                is Transaction.Received -> transaction.receiveAmount
            }
            val split = amountSentOrReceived.toString().split(".")
            val beforeDecimal = split[0]
            val afterDecimal = if (split[1].length > 1) split[1].substring(0,2) else split[1]
            val amountToShow = "$beforeDecimal.$afterDecimal"
            Column {
                Text(text = "$amountToShow ${assetToShow.name}")
                val text = if (received) "received" else "sent"
                Text(fontWeight = FontWeight(10), text = text)
            }
            Spacer(modifier = Modifier.width(100.dp))
            Column {
                val text = when (transaction) {
                    is Transaction.Received -> "From $recipientOrSender"
                    is Transaction.Sent -> "To $recipientOrSender"
                }
                Text(text = text)
                Text(fontWeight = FontWeight(10), text = DateFormat.getDateFormat(context).format(transaction.date))

            }
        }
    }
}
