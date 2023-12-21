package com.example.sanitas.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.sanitas.data.position.TravelRoute
import com.example.sanitas.data.position.TravelRouteDAO
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Database(entities = [TravelRoute::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LocalAppDatabase : RoomDatabase() {
    abstract fun travelRouteDao(): TravelRouteDAO

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: LocalAppDatabase? = null

        fun getDatabase(context: Context): LocalAppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalAppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class Converters {
    @TypeConverter
    fun fromLong(value: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
    }


    @TypeConverter
    fun toLong(date: LocalDateTime): Long {
        return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}