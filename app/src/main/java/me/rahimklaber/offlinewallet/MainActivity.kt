package me.rahimklaber.offlinewallet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.rahimklaber.offlinewallet.ui.theme.OfflineWalletTheme
import me.rahimklaber.offlinewallet.view.MainScreen
import org.stellar.sdk.KeyPair

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfflineWalletTheme {
                // A surface container using the 'background' color from the theme
                val seed = "SDGJHP5WUXNDQLRLBOMIM2TSD2JZWYYLTSM5JI7LYWUIT6SQ7XMNLXXA"
                val pubKey = "GBZTOCTK7UQXL2B7ABYWTLZDHCKN2YVZKBKPQJ75IL4YAYJ3OGE4FEFQ"
                val db = Room.databaseBuilder(
                    applicationContext,
                    me.rahimklaber.offlinewallet.db.Database::class.java,"nexus_db",
                ).fallbackToDestructiveMigration().build()
                val wallet = Wallet(keyPair = KeyPair.fromSecretSeed(seed),db,"coolman")
                Surface(color = MaterialTheme.colors.background) {
                   MainScreen(wallet = wallet)
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