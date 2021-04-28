package me.rahimklaber.offlinewallet.view

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.github.kittinunf.fuel.util.encodeBase64UrlToString
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.*
import me.rahimklaber.offlinewallet.Asset
import me.rahimklaber.offlinewallet.Wallet
import me.rahimklaber.offlinewallet.ui.theme.surfaceVariant
import java.util.*

/**
 * only for use with webview
 */
object Callback {
    var callback: (Array<Uri>?) -> Unit = {}
    operator fun invoke(uris: Array<Uri>?) {
        callback(uris)
    }
}

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
            var navigatingToDepositOrWithdraw by remember { mutableStateOf(false) }

            if (!navigatingToDepositOrWithdraw) {
                Card(modifier = modifier.padding(10.dp)) {
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        items(assets) {
                            Asset(
                                asset = it,
                                nav,
                                modifier.padding(5.dp),
                                { navigatingToDepositOrWithdraw = true },
                                { navigatingToDepositOrWithdraw = false })
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.padding(10.dp))
                }
            }
        }
        composable(
            "deposit/{asset}",
            arguments = listOf(navArgument("asset") { type = NavType.StringType })
        ) {
            var loading by remember { mutableStateOf(true) }
            val assetJson =
                it.arguments?.get("asset") as String
            var asset by remember { mutableStateOf<Asset.Custom?>(null) }
            LaunchedEffect(true) {
                loading = true

                asset =
                    withContext(Dispatchers.Default) {
                        parser.parse<Asset.Custom>(assetJson)
                    }
                loading = false
            }
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.padding(10.dp))
                }

            } else {
                Deposit(wallet, asset ?: throw Exception("parsing failed"), nav)
            }

        }
        composable(
            "interactivesession/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backentry ->
            var imageUri by remember { mutableStateOf(Uri.EMPTY) }

            val launcher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode != RESULT_OK) {
                        Callback(arrayOf(it.data?.data ?: Uri.EMPTY))
                    } else {
                        Callback(null)
                    }
                    //                it as Uri
//                imageUri = it
//                println(imageUri)
                }
            AndroidView(factory = {
                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webChromeClient = WebChromeClientWithFileUpload(launcher, Callback)
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.databaseEnabled = true
                    settings.domStorageEnabled = true
//                    settings.setSupportMultipleWindows(true)
                    println(backentry.arguments?.get("url"))
                    loadUrl(
                        (backentry.arguments?.get("url") as String).decodeBase64ToString()
                            ?: throw Exception("decoding of url failed")
                    )
                }

            })
        }
    }

}

/**
 * UI for depositing an asset using Sep-24
 */
@Composable
fun Deposit(
    wallet: Wallet,
    assetToDeposit: Asset.Custom,
    nav: NavController,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var authToken by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var doneLoading by remember { mutableStateOf(false) }
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
                scope.launch {
                    loading = true
                    authToken = wallet.getAuthToken(asset = assetToDeposit)
                    loading = false
                    doneLoading = true
                }
            }) {
                Text(text = "Start deposit process")
            }
            if (loading) {
                CircularProgressIndicator(Modifier.padding(5.dp))
            }
            var interactiveRequestDone by remember { mutableStateOf(false) }
            var interactiveDepositResponse by remember { mutableStateOf(JsonObject()) }
            if (doneLoading) {
                scope.launch(Dispatchers.Default) {
                    interactiveDepositResponse =
                        wallet.getInteractiveDepositSession(assetToDeposit, authToken)
                    interactiveRequestDone = true
                    launch(Dispatchers.IO) {
                        val asset = wallet.db.assetDao()
                            .getByNameAndIssuer(assetToDeposit.name, assetToDeposit.issuer)
                        val assetId = asset?.id ?: -1
                        println("asset id $assetId")
                        wallet.db.depositDao().addDeposit(
                            me.rahimklaber.offlinewallet.db.Deposit(
                                id = interactiveDepositResponse["id"] as String,
                                depositAssetId = assetId
                            )
                        )
                    }
                }
                if (interactiveRequestDone) {
                    val url = interactiveDepositResponse["url"] as String
                    val base64EncodedUrl = url.encodeBase64UrlToString()
                    println(url)
                    // for some reason passing in the full url doesn't work, it seems to stop at "?"
                    // so base64 encode it is
                    nav.navigate("interactivesession/$base64EncodedUrl")
                }
            }
        }
    }
}

/**
 * Element with represents the deposit or withdraw possibility for an Asset
 * @param onNavigateBegin called when navigating to either a deposit or withdraw screen
 * @param onNavigateEnd called when done with navigationg
 *
 * [onNavigateBegin] and [onNavigateEnd] are used to add a loading animation.
 */
@Composable
fun Asset(
    asset: Asset,
    nav: NavController,
    modifier: Modifier = Modifier,
    onNavigateBegin: () -> Unit = {},
    onNavigateEnd: () -> Unit = {}
) {
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
                    // Todo: the loading circle doesn't start right away, Probably has to do with calling onNavigateBegin in a coroutine
                    scope.launch(Dispatchers.Default) {
                        val assetJson = klaxon.toJsonString(asset)
                        onNavigateBegin()
                        withContext(Dispatchers.Main) {
                            nav.navigate("deposit/$assetJson")
                        }
                        onNavigateEnd()
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

class WebChromeClientWithFileUpload(
    val launcher: ActivityResultLauncher<Intent>,
    val callback: Callback
) : WebChromeClient() {
    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        try {
            callback.callback = { filePathCallback.onReceiveValue(it) }
            val intent = fileChooserParams.createIntent()
            launcher.launch(intent)
        } catch (e: java.lang.Exception) {
            println(e)
        }
        return true
    }
}
