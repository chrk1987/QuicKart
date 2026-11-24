package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        CartItemEntity::class,
        SavedAddressEntity::class,
        OrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuickKartDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao
    abstract fun savedAddressDao(): SavedAddressDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: QuickKartDatabase? = null

        fun getInstance(context: Context): QuickKartDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuickKartDatabase::class.java,
                    "quickkart_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
