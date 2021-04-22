package me.rahimklaber.offlinewallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * TODO: maybe only give a function for sending assets instead of the entire wallet?
 */
@Composable
fun SendOnlineQr(wallet: Wallet){

}

/**
 * send an asset to a user by user name
 */
@Composable
fun SendByUserName(wallet: Wallet, modifier: Modifier = Modifier){
    Card(modifier = modifier
        .fillMaxWidth(1f)
        .padding(10.dp)
    ) {

        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Send assets to user", fontSize = 26.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var nickname by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }
                var asset by remember { mutableStateOf(Asset.NOT_FOUND) } /*TODO: maybe replace NOT_FOUND by INVALID*/
                var assetsSupportedByRecipient by remember { mutableStateOf(listOf<Asset>())}


                val scope = rememberCoroutineScope()

                Text(text = "Nickname", fontWeight = FontWeight(50))
                TextField(value = nickname, onValueChange = { nickname = it })

                Button(onClick = {scope.launch(Dispatchers.IO){assetsSupportedByRecipient =  wallet.getAssetsForAccount(nickname)} }, modifier = Modifier.padding(5.dp)) {
                    Text("Load Assets")
                }

                Text(text = "Asset", fontWeight = FontWeight(50))
                DropdownAssetList(assets = assetsSupportedByRecipient, onSelectedAssetChanged = {asset = it})

                Text(text = "amount", fontWeight = FontWeight(50))
                TextField(value = amount, onValueChange = { amount = it })

                Text(text = "description", fontWeight = FontWeight(50))
                TextField(value = description, onValueChange = { description = it })
            }

        }
    }
}

@Composable
fun DropdownAssetList(assets : List<Asset>, onSelectedAssetChanged : (Asset)-> Unit){
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("Select asset to send") }
    var selectedAsset by remember { mutableStateOf(Asset.NOT_FOUND)}
    val icon = if (expanded)
        Icons.Filled.ArrowDropUp
    else
    Icons.Filled.ArrowDropDown


    Column {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            label = {Text("Label")},
            trailingIcon = {
                Icon(icon,"contentDescription", Modifier.clickable { expanded = !expanded })
            }
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