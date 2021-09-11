package networking

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

/**
 * Federation server resolver
 *
 * Note: can also be used to find the federated account given a stellar address
 * Note: You should not use this directly for now, untill I find a way to cache the result in the db.
 */
object AccountResolver
{
    val parser = Parser.default()
    private const val federationServerAddress = "https://cloudflare-federation-server.nexusfederation.workers.dev/"
    suspend operator fun invoke(nickname : String): String? = withContext(Dispatchers.IO){
        val response = federationServerAddress
            .httpGet(
                listOf(
                    "q" to "$nickname*stellar.galacticwizrd.space",
                    "type" to "name"
                )
            )
            .response()
        if(response.second.statusCode==404){
            return@withContext null
        }
        val json = parser.parse(ByteArrayInputStream(response.third.get())) as JsonObject
        (json["account_id"] as String).split("*")[0]

    }
}