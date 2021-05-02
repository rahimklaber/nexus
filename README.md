# Nexus

## Introduction
I have built a stablecoin wallet on Stellar called Nexus. It allows you to add deposit and withdraw stablecoins using SEP-24 and can therefore be used for cross-border payments. It removes the complexity of blockchain by using nicknames, which are federated addresses behind the scenes, instead of public keys.

## The idea

The Idea I had is to make a wallet which can be used for online- and point-of-sale payments and which can be used to quickly do cross-border payments. 

### Online payments
Where I am from, there is no way to easily pay online, most online payments happen via plain old bank-transfers. My idea is to create an anchor for the currency used and create a software library which would allow companies to easily accept online payments, which would be done by using the app. By leveraging Stellar companies could immidietly see when they receive payments and they could withdraw their assets using my anchor. To allow for quick deposits/withdrawals, I would setup bank accounts for the anchor at all of the banks.
### Point-Of-Sale
For point-of-sale payments, I was thinking the same as online payments; merchants can accept payment from the app and then withdraw the assets they received to their bank acounts, doing so they would be able to save on the large fees of card processors. One special thing I thought about is the possibility of having coupons/loyalty programs in the app. When people pay for an item, the coupons or loyalty points could be directly deposited to their wallet.

### Cross-border-payments
A lot of people from my country have relatives that live in another country. Sending money between the two countries can take days. Instead of using banks, they could use the wallet to send money. Theoretically, it would be possible to send money and receive it within a few minutes, since after receiving the assets, you could withdraw them and receive them in your bank account immidietly, depending on your anchor and bank. To facilitate this quick transfer, I would either setup an anchor in the second country or partner with an allready operating anchor.

## What I've built

I've build a stablecoin wallet where the complexity of blockchain is hidden by using federated addresses. The wallet allows you to add assets, withdraw/deposit these assets using sep-24 and send these assets to another wallet.

### Hiding complexity

When you first run the wallet, you will need to create an account by giving a nickname. This nickname is then associated with a stellar address via a federation server. Since the wallet uses the same federation server for everyone, the domain part of the federated address can be ommitted. In the future an option could be added to show the user his full address so the wallet can be interoperable with more advanced wallets.

Currently you can add assets which represent currencies in the real world, such as BRLT for Brazilean Real. In the future this would be changed to just show Brazilean real to remove any traces of the stellar block chain.

### Deposits/Withdrawals

You can deposit/withdraw funds in the wallet, this is achieved by using sep-24. When you want to deposit/withdraw a webpage is rendered in the wallet, you can then fill out your information and either send funds to the specified bank account to deposit or specify your bank account to withdraw.

### Cross-border payments

Using the wallet, it is possible to send assets to another user by specifying their nickname. Aside from being able to send a specific asset, it is also possible to specify the asset the recipient will receive and the assets the sender will send. This used path-payments in the background, and in combination with sep-24 it allows for cross-border payments.

### Eventual profitability
There are a few options on how to make money with the wallet.

 The first option is to charge a fee everytime someone wants to send money. While this would work, not many people would use the wallet if there are other alternatives which don't have fees. It also feels a bit morally wrong.
 
 The second option is to charge merchants when users pay with the wallet. It would be possible to charge them a very low fee due to the fact the we are using stellar. The merchants would be happy since they have to pay a normal fee than normal but a lot of merchants would need to accept the wallet for it to make profit.
 
 The third option is to use the wallet in conjunction with running an anchor. The idea would be to charge fees when users deposit or withdraw funds using the anchor. In case the wallet would be free to use, while users only need to pay to deposit or withdraw funds. The wallet would then act as a way to improve the revenue of anchors, so it might be possible to leverage that and make anchors pay us a small amount(the wallet's company) when people use the wallet with their anchor.



