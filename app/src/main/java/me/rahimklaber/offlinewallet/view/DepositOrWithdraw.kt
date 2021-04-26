package me.rahimklaber.offlinewallet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.*
import me.rahimklaber.offlinewallet.Asset
import me.rahimklaber.offlinewallet.Wallet
import me.rahimklaber.offlinewallet.ui.theme.surfaceVariant
import org.stellar.sdk.Network
import java.io.ByteArrayInputStream

/**
 * UI for selecting an asset to either withdraw from or deposit to.
 */
@Composable
fun DepositOrWithdrawScreen(wallet: Wallet, modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    val parser = remember { Klaxon() }
    val assets = wallet.assets
    val scope = rememberCoroutineScope()
    NavHost(navController = nav, startDestination = "depositOrWithdraw") {
        composable("depositOrWithdraw") {
            Card(modifier = modifier.padding(10.dp)) {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(assets) {
                        Asset(asset = it, nav, modifier.padding(5.dp))
                    }
                }
            }
        }
        composable(
            "deposit/{asset}",
            arguments = listOf(navArgument("asset") { type = NavType.StringType })
        ) {

            val assetJson =
                it.arguments?.get("asset") as String
            val asset =
                parser.parse<Asset.Custom>(assetJson)


            Deposit(wallet, asset ?: throw Exception("parsing failed"))
        }
    }

}

/**
 * UI for depositing an asset using Sep-24
 */
@Composable
fun Deposit(wallet: Wallet, assetToDeposit: Asset.Custom, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var authToken by remember { mutableStateOf("") }
    val parser by lazy { Parser.default() }
    Card(
        modifier = modifier
            .fillMaxWidth(1f)
            .padding(10.dp)
    ) {
        if (assetToDeposit.tomlString == null) {
            Text(text = "Something has gone wrong", Modifier.background(Color.Red))
            return@Card
        }
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                /**
                 * Todo: I should really handle errors.
                 */
                val authServerURl = assetToDeposit.toml.getString("AUTH_SERVER")
                scope.launch {
                    val authResponseBytes = withContext(Dispatchers.IO) {
                        authServerURl.httpGet(
                            listOf(
                                Pair("account", wallet.account.accountId)
                            )
                        ).response().third.component1()
                    }

                    val parsedAuthResponse =
                        withContext(Dispatchers.Default) {

                            parser.parse(ByteArrayInputStream(authResponseBytes)) as JsonObject
                        }
                    val transactionXdr = parsedAuthResponse["transaction"] as String
                    val networkPassphrase = parsedAuthResponse["network_passphrase"] as String
                    val network = Network(networkPassphrase)
                    val txToSign =
                        org.stellar.sdk.Transaction.fromEnvelopeXdr(transactionXdr,network)
                    txToSign.sign(wallet.keyPair)
                    val authTokenResponseBytes = withContext(Dispatchers.IO){
                        authServerURl.httpPost(listOf(Pair("",txToSign.toEnvelopeXdrBase64())))
                            .response().third.get()
                    }
                    authToken = (parser.parse(ByteArrayInputStream(authTokenResponseBytes)) as JsonObject)["token"] as String



            }) {
                Text(text = "Start deposit process")
            }
        }
    }
}


@Composable
fun Asset(asset: Asset, nav: NavController, modifier: Modifier = Modifier) {
    val klaxon = Klaxon()
    val scope = rememberCoroutineScope()
    Card(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surfaceVariant
    ) {
        Column {
            Row(modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                CoilImage(
                    imageModel = asset.iconLink
                        ?: Icons.Default.Place /*TODO this doesnt work*/,
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .padding(5.dp),
                    alignment = Alignment.Center

                ) {
                    Text(text = asset.name)
                }
                Text(text = asset.name)
            }
            Row(modifier.padding(5.dp)) {
                Button(onClick = {
                    scope.launch {
                        val assetJson = klaxon.toJsonString(asset)
                        nav.navigate("deposit/$assetJson")
                    }
                }, Modifier.padding(5.dp)) {
                    Text("deposit")
                }
                Button(onClick = { /*TODO*/ }, Modifier.padding(5.dp)) {
                    Text("withdraw")
                }
            }
        }

    }
}

