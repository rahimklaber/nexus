package db

import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select

class AssetDao(val db: Database) {
    fun getAllAssets() : List<AssetModel>{
        return db.from(AssetTable).select().map
    }
}