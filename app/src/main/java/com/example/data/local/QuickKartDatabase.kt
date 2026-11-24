package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        CartItemEntity::class,
        SavedAddressEntity::class,
        OrderEntity::class,
        SupportTicketEntity::class,
        WalletTransactionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class QuickKartDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao
    abstract fun savedAddressDao(): SavedAddressDao
    abstract fun orderDao(): OrderDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: QuickKartDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE orders ADD COLUMN deliveryWindowLabel TEXT NOT NULL DEFAULT 'Express • 8 mins'"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `support_tickets` (
                        `ticketId` TEXT NOT NULL,
                        `orderId` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `affectedItemsSummary` TEXT NOT NULL,
                        `affectedAmountInr` INTEGER NOT NULL,
                        `preferredResolutionName` TEXT NOT NULL,
                        `resolutionSummary` TEXT NOT NULL,
                        `statusName` TEXT NOT NULL,
                        `photoUriString` TEXT,
                        `messagesJson` TEXT NOT NULL,
                        `csatRating` INTEGER,
                        PRIMARY KEY(`ticketId`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_addresses ADD COLUMN area TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_addresses ADD COLUMN city TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_addresses ADD COLUMN state TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_addresses ADD COLUMN postalCode TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE saved_addresses ADD COLUMN country TEXT NOT NULL DEFAULT 'India'")
                db.execSQL("ALTER TABLE saved_addresses ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE saved_addresses ADD COLUMN longitude REAL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `wallet_transactions` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `amountInr` INTEGER NOT NULL,
                        `isCredit` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): QuickKartDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuickKartDatabase::class.java,
                    "quickkart_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
