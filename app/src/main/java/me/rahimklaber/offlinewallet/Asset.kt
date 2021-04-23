package me.rahimklaber.offlinewallet

import org.stellar.sdk.AssetTypeNative

/**
 * Class to represent an Stellar asset
 *
 * iconlink can be either a link to an icon are a drawable id, Todo pick a better variable name
 */
sealed class Asset{
    abstract val iconLink : Any?
    abstract val name: String

    abstract fun toStellarSdkAsset(): org.stellar.sdk.Asset

    object Native : Asset() {
        override val iconLink: Any
            get() = R.drawable.xlm_logo
        override val name: String
            get() = "xlm"

        override fun toStellarSdkAsset(): AssetTypeNative {
            return AssetTypeNative()
        }
    }

    data class Custom(override val name: String, val issuer: String, override val iconLink : Any?) : Asset() {
        override fun toStellarSdkAsset(): org.stellar.sdk.Asset {
            return org.stellar.sdk.Asset.createNonNativeAsset(name,issuer)
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

    companion object{

        val NOT_FOUND : Asset = Custom("NOTFOUND","",null)
    }
}