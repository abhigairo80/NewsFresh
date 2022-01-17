package com.complete.newsfresh.database

import androidx.room.TypeConverter
import com.complete.newsfresh.model.Source

class Converters {
    @TypeConverter
    fun fromSource(source: Source):String{
        return source.name
    }
    @TypeConverter
    fun toSource(name :String): Source {
        return Source(name,name)
    }
}