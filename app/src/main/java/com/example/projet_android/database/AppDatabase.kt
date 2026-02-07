package com.example.projet_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projet_android.dao.CompanyDao
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.dao.OrderDao
import com.example.projet_android.dao.OrderProductDao
import com.example.projet_android.dao.ProductDao
import com.example.projet_android.model.Company
import com.example.projet_android.model.Container
import com.example.projet_android.model.Order
import com.example.projet_android.model.OrderProduct
import com.example.projet_android.model.Product

@Database(
    entities = [
        Company::class,
        Product::class,
        Order::class,
        OrderProduct::class,
        Container::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderProductDao(): OrderProductDao
    abstract fun containerDao(): ContainerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_management_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
