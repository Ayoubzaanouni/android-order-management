package com.example.projet_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projet_android.dao.CompanyDao
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.dao.OrderDao
import com.example.projet_android.dao.OrderProductDao
import com.example.projet_android.dao.ProductDao
import com.example.projet_android.dao.ContainerOrderDao
import com.example.projet_android.dao.EmployeeDao
import com.example.projet_android.dao.AdminDao
import com.example.projet_android.dao.ShippingPlanDao
import com.example.projet_android.model.Company
import com.example.projet_android.model.Container
import com.example.projet_android.model.Order
import com.example.projet_android.model.OrderProduct
import com.example.projet_android.model.Product
import com.example.projet_android.model.ContainerOrder
import com.example.projet_android.model.Employee
import com.example.projet_android.model.Admin
import com.example.projet_android.model.ShippingPlan
import com.example.projet_android.model.ShippingPlanProduct
import com.example.projet_android.util.PasswordUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Company::class,
        Product::class,
        Order::class,
        OrderProduct::class,
        Container::class,
        ContainerOrder::class,
        Employee::class,
        Admin::class,
        ShippingPlan::class,
        ShippingPlanProduct::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderProductDao(): OrderProductDao
    abstract fun containerDao(): ContainerDao
    abstract fun containerOrderDao(): ContainerOrderDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun adminDao(): AdminDao
    abstract fun shippingPlanDao(): ShippingPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Alter company table — add new columns
                database.execSQL("ALTER TABLE company ADD COLUMN phone TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE company ADD COLUMN status TEXT NOT NULL DEFAULT 'APPROVED'")

                // Create employee table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `employee` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `companyId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `password` TEXT NOT NULL,
                        FOREIGN KEY(`companyId`) REFERENCES `company`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_employee_companyId` ON `employee` (`companyId`)")

                // Create admin table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `admin` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `username` TEXT NOT NULL,
                        `password` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shipping_plan` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `totalRevenue` REAL NOT NULL,
                        `totalProductsShipped` INTEGER NOT NULL,
                        `totalProductsPostponed` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shipping_plan_product` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `planId` INTEGER NOT NULL,
                        `containerId` INTEGER NOT NULL,
                        `orderProductId` INTEGER NOT NULL,
                        `shipped` INTEGER NOT NULL,
                        FOREIGN KEY(`planId`) REFERENCES `shipping_plan`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`containerId`) REFERENCES `container`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`orderProductId`) REFERENCES `order_product`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_shipping_plan_product_planId` ON `shipping_plan_product` (`planId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_shipping_plan_product_containerId` ON `shipping_plan_product` (`containerId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_shipping_plan_product_orderProductId` ON `shipping_plan_product` (`orderProductId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_management_database"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.adminDao()?.insert(
                                    Admin(
                                        username = "admin",
                                        password = PasswordUtils.hash("admin123")
                                    )
                                )
                            }
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = INSTANCE?.adminDao() ?: return@launch
                                if (dao.getByUsername("admin") == null) {
                                    dao.insert(
                                        Admin(
                                            username = "admin",
                                            password = PasswordUtils.hash("admin123")
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

