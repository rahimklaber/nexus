package db

import org.ktorm.entity.Entity

interface AccountModel : Entity<AccountModel>{
    companion object : Entity.Factory<AccountModel>()
    val nickname : String
    val privateKey: String
}

interface UserModel : Entity<UserModel>{
    companion object : Entity.Factory<UserModel>()

    val publicKey : String
    val nickname : String
}

interface AssetModel : Entity<AssetModel> {
    companion object : Entity.Factory<AssetModel>()

    val id : Int
    val name : String
    val issuer : String
    val authToken : String
    val icon_link : String
}