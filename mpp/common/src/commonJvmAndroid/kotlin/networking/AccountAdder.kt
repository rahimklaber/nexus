package networking

import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AccountAdder {
    private const val federationServerAddress =
        "https://cloudflare-federation-server.nexusfederation.workers.dev/"

    suspend operator fun invoke(nickname: String, address: String) = withContext(Dispatchers.IO) {
        val json = """
            {
                "nickname" : "$nickname*stellar.galacticwizrd.space",
                "address" : "$address"
            }
        """.trimIndent()
        federationServerAddress
            .httpPost()
            .jsonBody(json)
            .responseString().third.get()

    }
}