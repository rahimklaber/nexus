package me.rahimklaber.offlinewallet.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rahimklaber.offlinewallet.Asset
import me.rahimklaber.offlinewallet.Wallet

/**
 * TODO: maybe only give a function for sending assets instead of the entire wallet?
 */
@Composable
fun SendOnlineQr(wallet: Wallet) {

}

/**
 * send an asset to a user by user name
 */
@Composable
fun SendByUserName(wallet: Wallet, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(1f)
            .padding(10.dp)
    ) {

        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Send assets to user", fontSize = 26.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var nickname by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }
                var receiveAsset by remember { mutableStateOf(Asset.NOT_FOUND) } /*TODO: maybe replace NOT_FOUND by INVALID*/
                var assetsSupportedByRecipient by remember { mutableStateOf(listOf<Asset>()) }
                var sendingAsset by remember { mutableStateOf(Asset.NOT_FOUND) }
                var loading by remember { mutableStateOf(false) }

                val scope = rememberCoroutineScope()

                Text(text = "Nickname", fontWeight = FontWeight(50))
                TextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )

                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        assetsSupportedByRecipient = wallet.getAssetsForAccount(nickname)
                    }
                }, modifier = Modifier.padding(5.dp)) {
                    Text("Load Assets")
                }

                Text(text = "Recipient asset", fontWeight = FontWeight(50))
                DropdownAssetList(
                    text = "select the asset that the recipient will receive",
                    assets = assetsSupportedByRecipient,
                    onSelectedAssetChanged = { receiveAsset = it })

                Text(text = "Asset to send", fontWeight = FontWeight(50))
                DropdownAssetList(
                    text = "Select asset to send",
                    assets = wallet.assetsBalances.keys.toList(),
                    onSelectedAssetChanged = { sendingAsset = it })


                Text(text = "amount (of recipient asset)", fontWeight = FontWeight(50))
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )

                Text(text = "description", fontWeight = FontWeight(50))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )

                )



                Spacer(modifier = Modifier.height(20.dp))
                val context = LocalContext.current
                Button(onClick = {
                    scope.launch {
                        loading = true
                        val response = withContext(scope.coroutineContext) {
                            wallet.sendAssetAsync(
                                nickname,
                                receiveAsset,
                                sendingAsset,
                                amount,
                                description
                            )
                        }.await()

                        val succeededOrFailed = if (response.isSuccess) "Succeeded" else "Failed"
                        Toast.makeText(
                            context,
                            "Transaction $succeededOrFailed",
                            Toast.LENGTH_SHORT
                        ).show()

                        loading = false

                    }

                }) {
                    Text(text = "Send")
                }
                if (loading)

                    CircularProgressIndicator(Modifier.padding(5.dp))


            }

        }
    }
}

@Composable
fun DropdownAssetList(text: String, assets: List<Asset>, onSelectedAssetChanged: (Asset) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(text) }
    var selectedAsset by remember { mutableStateOf(Asset.NOT_FOUND) }
    val icon = if (expanded)
        Icons.Filled.ArrowDropUp
    else
        Icons.Filled.ArrowDropDown


    Column {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("asset") },
            trailingIcon = {
                Icon(icon, "contentDescription", Modifier.clickable { expanded = !expanded })
            },
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            assets.forEach { asset ->
                DropdownMenuItem(onClick = {
                    selectedText = asset.name
                    selectedAsset = asset
                    onSelectedAssetChanged(selectedAsset)
                }) {
                    Row {
                        CoilImage(
                            imageModel = asset.iconLink ?: "xd"/*just so this will fail*/,
                            modifier = Modifier
                                .height(50.dp)
                                .width(50.dp)
                        ) {
                            Text(text = asset.name)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(text = asset.name)
                    }
                }
            }
        }
    }
}