package me.rahimklaber.offlinewallet.view

import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rahimklaber.offlinewallet.Asset
import me.rahimklaber.offlinewallet.Wallet
import java.io.File
import kotlin.math.absoluteValue
import kotlin.random.Random

@Composable
fun CreateQrForReceiveScreen(wallet: Wallet,modifier: Modifier = Modifier){
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){}
    Card(
        modifier = modifier
            .fillMaxWidth(1f)
            .padding(10.dp)
    ) {

        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create Qr code", fontSize = 26.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                var amount by remember { mutableStateOf("") }
                var receiveAsset by remember { mutableStateOf<Asset.Custom>(Asset.NOT_FOUND as Asset.Custom) } /*TODO: maybe replace NOT_FOUND by INVALID*/
                var loading by remember { mutableStateOf(false) }

                val scope = rememberCoroutineScope()


                Text(text = "asset", fontWeight = FontWeight(50))
                DropdownAssetList(
                    text = "select the asset you want to receive",
                    assets = wallet.assets,
                    onSelectedAssetChanged = { receiveAsset = it as Asset.Custom })

                Text(text = "amount to receive", fontWeight = FontWeight(50))
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )



                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    scope.launch {
                        val builder = StrictMode.VmPolicy.Builder()
                        StrictMode.setVmPolicy(builder.build())
                        val link = "web+stellar:pay?destination=${wallet.user.name}*stellar.galacticwizrd.space&amount=$amount&asset_code=${receiveAsset.name}&asset_issuer=${receiveAsset.issuer}"
                        println(link)
                        val qrEncoder = QRGEncoder(link,null,QRGContents.Type.TEXT,500)
                        qrEncoder.colorBlack
                        val bitmap = qrEncoder.bitmap
                        val imageName = link.hashCode().absoluteValue.toString()
                        println(context.cacheDir.canonicalPath)
                        QRGSaver().save("${context.cacheDir.canonicalFile}/", imageName,qrEncoder.bitmap, QRGContents.ImageType.IMAGE_JPEG)
                        val file = File("${context.cacheDir.canonicalPath}/$imageName.jpg")

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                            type ="image/jpeg"
                        }
                        val shareIntent = Intent.createChooser(sendIntent,null)
                        launcher.launch(shareIntent)


                    }

                }) {
                    Text(text = "create Qr code")
                }
                if (loading)

                    CircularProgressIndicator(Modifier.padding(5.dp))


            }

        }
    }
}