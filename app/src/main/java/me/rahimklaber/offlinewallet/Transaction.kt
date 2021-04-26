package me.rahimklaber.offlinewallet

import java.util.*

/**
 * the meaning of [sendAsset] and [receiveAsset] depend on the transaction type.
 *
 * If [Transaction.Received] then [sendAsset] is the asset which the sender send while [receiveAsset]
 * is the asset that you received.
 *
 * if [Transaction.Sent] then [sendAsset] is the asset you sent, while [receiveAsset] is the asset
 * the recipient received.
 */
sealed class Transaction {
    /**
     * the operation id
     */
    abstract val id : Long
    abstract val sendAsset: Asset
    abstract val receiveAsset : Asset
    abstract val sendAmount: Float
    abstract val receiveAmount : Float
    abstract val date: Date
    abstract val description: String

    /**
     * True if the operation is a path payment
     */
    abstract val pathPayment : Boolean

    /**
     * [sendAsset] is the asset which the sender send while [receiveAsset]
     * is the asset that you received.
     *
     * [sendAmount] is the amount of the sendAsset the sender sent.
     *
     * [receiveAmount] is the amount of the receiveAsset that you received
     */
    class Received(
        override val id: Long,
        val from: Account,
        override val sendAsset: Asset,
        override val receiveAsset: Asset,
        override val sendAmount: Float,
        override val receiveAmount: Float,
        override val date: Date,
        override val description: String,
        override val pathPayment: Boolean
    ) : Transaction()

    /**
     * [sendAsset] is the asset you sent, while [receiveAsset] is the asset
     * the recipient received.
     *
     * [sendAmount] is the amount of the sendAsset you sent
     *
     * [receiveAmount] is the amount of the receiveAsset that the recipient receives
     */
    class Sent(
        override val id: Long,
        val recipient: Account,
        override val sendAsset: Asset,
        override val receiveAsset: Asset,
        override val sendAmount: Float,
        override val receiveAmount: Float,
        override val date: Date,
        override val description: String,
        override val pathPayment: Boolean
    ) : Transaction()
}
