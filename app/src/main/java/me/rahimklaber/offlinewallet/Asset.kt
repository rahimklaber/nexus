package me.rahimklaber.offlinewallet

sealed class Asset{
    object Native : Asset()
    data class Custom(val name: String, val issuer: String) : Asset()
}