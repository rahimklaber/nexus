package me.rahimklaber.offlinewallet.db

import androidx.room.TypeConverter
import java.util.*

class Converters{
    @TypeConverter
    fun toDate(dateLong : Long?):Date?{
        return if(dateLong == null){
            null
        }else{
            Date(dateLong)
        }
    }
    @TypeConverter
    fun fromDate(date : Date?): Long?{
        return if(date==null){
            null
        }else{
            return date.time
        }
    }
}