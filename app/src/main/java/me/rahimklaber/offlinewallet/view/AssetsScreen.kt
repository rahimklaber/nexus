package me.rahimklaber.offlinewallet.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rahimklaber.offlinewallet.Asset
import me.rahimklaber.offlinewallet.Wallet
import me.rahimklaber.offlinewallet.ui.theme.surfaceVariant

val assets = listOf(
    me.rahimklaber.offlinewallet.Asset.Custom(
        "TZS",
        "GAH572DYUPXZDOKBI76H54WRKMIHDXZFLOFVFBDPKL3WIUTPGGHCQ5K7",
        "https://res.cloudinary.com/clickpesa/image/upload/v1603170740/assets/clickpesa-icon.png"
    ),
    Asset.Custom(
        "BRLT",
        "GB7TAYRUZGE6TVT7NHP5SMIZRNQA6PLM423EYISAOAP3MKYIQMVYP2JO",
        "https://testnet-sep.stablex.cloud/brlt-logo.png"
    ) ,   Asset.Custom(
        "ARST",
        "GB7TAYRUZGE6TVT7NHP5SMIZRNQA6PLM423EYISAOAP3MKYIQMVYP2JO",
        "https://testnet-sep.stablex.cloud/arst-logo.png"
    )
)


/**
 * screen where you can view and add available assets.
 */
@Composable
fun AssetsScreen(wallet: Wallet, modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(10.dp)) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(10.dp)
        ) {
            items(assets) {
                Asset(
                    asset = it,
                    wallet,
                    Modifier.padding(5.dp)
                )
            }
        }
    }
}

/**
 * Represents an asset card with a button that you can click to add the asset.
 */
@Composable
fun Asset(asset: Asset.Custom, wallet: Wallet, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
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
                if (!loading) {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.Default) {
                                loading = true
                                val response = wallet.addAssetAsync(assetToAdd = asset).await()
                                if (response.isSuccess) {
                                    withContext(Dispatchers.IO) {
                                        wallet.updateBalance()
                                    }
                                }
                                val succeededOrFailed =
                                    if (response.isSuccess) "succeeded" else "Failed"
                                launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "$succeededOrFailed adding asset",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                loading = false
                            }
                        },
                        Modifier.padding(5.dp),
                        enabled = !wallet.assetsBalances.keys.contains(asset)
                    ) {
                        Text("add asset")
                    }
                } else {
                    CircularProgressIndicator(Modifier.padding(10.dp))
                }
            }
        }


    }
}