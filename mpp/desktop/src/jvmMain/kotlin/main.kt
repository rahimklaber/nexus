import me.rahim.common.App
import androidx.compose.desktop.Window
import db.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.stellar.sdk.KeyPair

fun main(): Unit = run { //    App()
    val db = Database.connect("jdbc:sqlite:db.sqlite")
    println(db.from(AssetTable).select().map(AssetTable::createEntity).first())


}
