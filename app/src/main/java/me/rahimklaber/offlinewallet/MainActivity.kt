package me.rahimklaber.offlinewallet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.rahimklaber.offlinewallet.db.Account
import me.rahimklaber.offlinewallet.db.User
import me.rahimklaber.offlinewallet.networking.AccountAdder
import me.rahimklaber.offlinewallet.ui.theme.OfflineWalletTheme
import me.rahimklaber.offlinewallet.view.MainScreen
import org.stellar.sdk.KeyPair

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfflineWalletTheme {
                // A surface container using the 'background' color from the theme
//                val seed = "SBIBTPLJQPCI3M4J2WJ3NFNVEZZLQGTBN47CDNDKO72OAMUNCPEJDRTR"
//                val pubKey = "GCPUOL3H74I5YFHEJSC6A5O23O3HJ6BJVELQND4FW6VAKPZ3RX4UHWZX"
                val db = Room.databaseBuilder(
                    applicationContext,
                    me.rahimklaber.offlinewallet.db.Database::class.java, "nexus4_db",
                ).fallbackToDestructiveMigration().build()

                var wallet: Wallet? =null
                Surface(color = MaterialTheme.colors.background) {
                    val account = runBlocking(Dispatchers.IO) {
                        db.accountDao().get()
                    }
                    if(account != null){
                        wallet = Wallet(KeyPair.fromSecretSeed(account.privateKey),db,account.nickName)
                    }
                    var createdAccount by remember { mutableStateOf(false) }
                    if (account == null && !createdAccount) {
                        var nickname by remember { mutableStateOf("") }
                        var loading by remember { mutableStateOf(false) }

                        val scope = rememberCoroutineScope()
                        Card(Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Create an Account",
                                    color = MaterialTheme.colors.primary,
                                    fontSize = 33.sp,
                                    modifier = Modifier.padding(top = 100.dp)
                                )
                                OutlinedTextField(
                                    value = nickname,
                                    onValueChange = { nickname = it },
                                    modifier = Modifier.padding(top = 30.dp),
                                    label = { Text("nickname") })
                                Button(onClick = {
                                    scope.launch(Dispatchers.Default){
                                        loading = true
                                        val keyPair = KeyPair.random()
                                        val secret = keyPair.secretSeed
                                        val publicKey = keyPair.accountId

                                        withContext(Dispatchers.IO){
                                            db.accountDao().addAccount(Account(nickname,String(secret)))
                                            "https://friendbot.stellar.org?addr=$publicKey"
                                                .httpGet()
                                                .responseString()
                                            AccountAdder(nickname = nickname,address = publicKey)

                                        }
                                        wallet = Wallet(keyPair = keyPair,db,nickname)
                                        createdAccount = true
                                        loading = false
                                    }

                                }, modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Create Account")
                                }
                                if(loading){
                                    CircularProgressIndicator(Modifier.padding(5.dp))
                                }
                            }
                        }
                    } else {
                        MainScreen(wallet = wallet!!)
                    }
                }

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    OfflineWalletTheme {
//        MainScreen(wallet = )
    }
}