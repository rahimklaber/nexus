package me.rahimklaber.offlinewallet

import java.util.*

sealed class Transaction {
    abstract val asset: Asset
    abstract val amount: Float
    abstract val date: Date
    abstract val description: String
    class Received(
        val from: Account, override val asset: Asset,
        override val amount: Float,
        override val date: Date,
        override val description: String
    ) : Transaction()

    class Sent(
        val recipient: Account, override val asset: Asset,
        override val amount: Float,
        override val date: Date,
        override val description: String
    ) : Transaction()
}

//Todo use sealed class for received and sent.