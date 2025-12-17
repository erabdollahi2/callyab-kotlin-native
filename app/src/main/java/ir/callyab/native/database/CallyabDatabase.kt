package ir.callyab.native.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.callyab.native.models.*

/**
 * پایگاه داده اصلی CallyAB
 * معادل دقیق Flutter DatabaseInitializer + sqflite setup
 */
@Database(
    entities = [
        Contact::class,
        SearchRequest::class,
        PaymentTransaction::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CallyabDatabase : RoomDatabase() {
    
    abstract fun contactDao(): ContactDao
    abstract fun searchRequestDao(): SearchRequestDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: CallyabDatabase? = null

        /** نام پایگاه داده - معادل Flutter _databaseName */
        private const val DATABASE_NAME = "contacts.db"

        /**
         * دریافت instance پایگاه داده (Singleton pattern)
         * معادل دقیق Flutter DatabaseInitializer.initializeDatabase()
         */
        fun getDatabase(context: Context): CallyabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallyabDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * بستن پایگاه داده
         */
        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
    
    /**
     * Callback برای عملیات پس از ایجاد پایگاه داده
     * معادل Flutter DatabaseInitializer onCreate
     */
    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            // ایجاد Index های اضافی برای بهبود سرعت جستجو
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_contact_full_name ON contacts(full_name)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_contact_created_at ON contacts(created_at)")
            
            // Log موفقیت ایجاد پایگاه داده
            println("✅ پایگاه داده CallyAB ایجاد شد")
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            println("🔄 پایگاه داده CallyAB باز شد")
        }
    }
}

/**
 * کلاس کمکی برای مقداردهی اولیه پایگاه داده
 * معادل دقیق Flutter DatabaseInitializer
 */
object DatabaseInitializer {
    
    /**
     * مقداردهی پایگاه داده با داده‌های نمونه
     * معادل دقیق Flutter DatabaseInitializer.initializeDatabase()
     */
    suspend fun initializeWithSampleData(context: Context) {
        val database = CallyabDatabase.getDatabase(context)
        val contactDao = database.contactDao()
        
        // بررسی اینکه آیا داده‌ای وجود دارد
        val count = contactDao.getTotalCount()
        
        if (count == 0) {
            println("📝 پایگاه داده خالی است - بارگیری داده‌های نمونه...")
            
            // اضافه کردن داده‌های نمونه
            val sampleContacts = listOf(
                Contact(
                    nationalCode = "1234567890",
                    cardNo = "1234567890123456",
                    fullName = "احمد محمدی",
                    mobile = "09123456789",
                    birthDate = "1370/01/01"
                ),
                Contact(
                    nationalCode = "0987654321",
                    cardNo = "6543210987654321",
                    fullName = "مریم احمدی",
                    mobile = "09987654321",
                    birthDate = "1375/05/15"
                ),
                Contact(
                    nationalCode = "1122334455",
                    cardNo = "1122334455667788",
                    fullName = "علی رضایی",
                    mobile = "09112233445",
                    birthDate = "1368/12/10"
                )
            )
            
            contactDao.insertContacts(sampleContacts)
            println("✅ ${sampleContacts.size} مخاطب نمونه اضافه شد")
        } else {
            println("✅ پایگاه داده آماده - تعداد رکوردها: $count")
        }
    }
    
    /**
     * دریافت آمار پایگاه داده
     * معادل Flutter getAssetDatabaseInfo()
     */
    suspend fun getDatabaseInfo(context: Context): Map<String, Any> {
        val database = CallyabDatabase.getDatabase(context)
        val contactDao = database.contactDao()
        val stats = contactDao.getStatistics()
        
        return mapOf(
            "totalRecords" to stats.total,
            "withName" to stats.withName,
            "withMobile" to stats.withMobile,
            "withBirthDate" to stats.withBirthDate,
            "completenessPercentage" to if (stats.total > 0) (stats.withName * 100 / stats.total) else 0
        )
    }
}