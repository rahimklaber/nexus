package me.rahimklaber.offlinewallet

import com.beust.klaxon.Json
import com.moandjiezana.toml.Toml
import org.stellar.sdk.AssetTypeCreditAlphaNum
import org.stellar.sdk.AssetTypeNative

/**
 * Class to represent an Stellar asset
 *
 * iconlink can be either a link to an icon are a drawable id, Todo pick a better variable name
 */
sealed class Asset {
    abstract var iconLink: Any?
    abstract val name: String
    abstract val tomlString: String?
    abstract fun toStellarSdkAsset(): org.stellar.sdk.Asset

    /**
     * Might change this in the future, but for now
     * we assume that we only deal with stable-coins.
     */
    val decimalsToShow = 2


    object Native : Asset() {
        override var iconLink: Any? = R.drawable.xlm_logo
        override val name: String
            get() = "xlm"
        override val tomlString: String? = null

        override fun toStellarSdkAsset(): AssetTypeNative {
            return AssetTypeNative()
        }
    }

    data class Custom(
        override val name: String,
        val issuer: String,
        override var iconLink: Any? = null,
        override val tomlString: String? = null
    ) : Asset() {
        // Doing this to make serialization easier.
        @Json(ignored=true)
        val toml : Toml by lazy {Toml().read(tomlString)}
        override fun toStellarSdkAsset(): org.stellar.sdk.Asset {
            return org.stellar.sdk.Asset.createNonNativeAsset(name, issuer)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Custom

            if (name != other.name) return false
            if (issuer != other.issuer) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + issuer.hashCode()
            return result
        }

    }

    companion object {
        val NOT_FOUND: Asset = Custom("NOTFOUND", "", null,null)

        fun fromSdkAsset(sdkAsset: org.stellar.sdk.Asset): Asset {
            return when (sdkAsset) {
                is AssetTypeNative -> Native
                is AssetTypeCreditAlphaNum -> Custom(sdkAsset.code, sdkAsset.issuer, null,null)
                else -> throw Exception("Asset type not supported")
            }
        }
    }
}