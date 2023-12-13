package com.example.sanitas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import com.example.sanitas.data.position.TravelRoute
import com.example.sanitas.data.position.TravelRouteDAO
import com.here.sdk.core.GeoCoordinates
import kotlinx.coroutines.CoroutineScope

@Database(entities = [TravelRoute::class], version = 1, exportSchema = false)
public abstract class LocalAppDatabase : RoomDatabase() {
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
