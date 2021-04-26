package me.rahimklaber.offlinewallet

abstract class Account(val name: String) {

}

class  User (name: String, publicKey : String? = null) : Account(name)
