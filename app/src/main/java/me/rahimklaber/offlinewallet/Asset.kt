package me.rahimklaber.offlinewallet

/**
 * Class to represent an Stellar asset
 *
 * iconlink can be either a link to an icon are a drawable id, Todo pick a better variable name
 */
sealed class Asset{
    abstract val iconLink : Any?
    abstract val name: String
    object Native : Asset() {
        override val iconLink: Any
            get() = R.drawable.xlm_logo
        override val name: String
            get() = "xlm"
    }

    data class Custom(override val name: String, val issuer: String, override val iconLink : Any?) : Asset()
    companion object{

        val NOT_FOUND : Asset = Custom("NOTFOUND","",null)
    }
}