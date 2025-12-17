package ir.callyab.native.services

import android.content.Context
import kotlinx.coroutines.*
import ir.callyab.native.database.CallyabDatabase
import ir.callyab.native.database.ContactDao
import ir.callyab.native.models.Contact

/**
 * سرویس یکپارچه مدیریت مخاطبین 
 * معادل دقیق Flutter UniversalContactService
 */
class UniversalContactService private constructor(context: Context) {
    
    private val contactDao: ContactDao = CallyabDatabase.getDatabase(context).contactDao()
    
    // سیستم کش برای عملکرد بهتر - معادل Flutter _cachedContacts
    private var cachedContacts: List<Contact>? = null
    private var lastCacheTime: Long = 0
    private val cacheValidDuration = 5 * 60 * 1000L // 5 minutes in milliseconds
    
    companion object {
        @Volatile
        private var INSTANCE: UniversalContactService? = null
        
        fun getInstance(context: Context): UniversalContactService {
            return INSTANCE ?: synchronized(this) {
                val instance = UniversalContactService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * تعداد کل مخاطبین با کش بهینه
     * معادل دقیق Flutter getTotalCount()
     */
    suspend fun getTotalCount(): Int = withContext(Dispatchers.IO) {
        try {
            // بررسی کش معتبر
            if (cachedContacts != null && isCacheValid()) {
                return@withContext cachedContacts!!.size
            }
            
            // دریافت از پایگاه داده
            val count = contactDao.getTotalCount()
            return@withContext count
        } catch (e: Exception) {
            println("خطا در دریافت تعداد: $e")
            // در صورت خطا، از کش قدیمی استفاده کن
            return@withContext cachedContacts?.size ?: 0
        }
    }
    
    /**
     * جستجو در مخاطبین با کش بهینه
     * معادل دقیق Flutter searchContacts()
     */
    suspend fun searchContacts(query: String, limit: Int = 50): List<Contact> = withContext(Dispatchers.IO) {
        try {
            return@withContext contactDao.searchContacts(query, limit)
        } catch (e: Exception) {
            println("خطا در جستجو: $e")
            return@withContext emptyList()
        }
    }
    
    /**
     * جستجو بر اساس کد ملی
     * معادل دقیق Flutter searchContactByNationalCode()
     */
    suspend fun searchContactByNationalCode(nationalCode: String): Contact? = withContext(Dispatchers.IO) {
        try {
            return@withContext contactDao.findByNationalCode(nationalCode)
        } catch (e: Exception) {
            println("خطا در جستجو بر اساس کد ملی: $e")
            return@withContext null
        }
    }
    
    /**
     * جستجو بر اساس شماره موبایل
     * معادل دقیق Flutter searchContactByMobile()
     */
    suspend fun searchContactByMobile(mobile: String): Contact? = withContext(Dispatchers.IO) {
        try {
            return@withContext contactDao.findByMobile(mobile)
        } catch (e: Exception) {
            println("خطا در جستجو بر اساس موبایل: $e")
            return@withContext null
        }
    }
    
    /**
     * جستجو بر اساس شماره کارت
     * معادل دقیق Flutter searchContactByCardNo()
     */
    suspend fun searchContactByCardNo(cardNo: String): Contact? = withContext(Dispatchers.IO) {
        try {
            return@withContext contactDao.findByCardNo(cardNo)
        } catch (e: Exception) {
            println("خطا در جستجو بر اساس شماره کارت: $e")
            return@withContext null
        }
    }
    
    /**
     * دریافت تمام مخاطبین با صفحه‌بندی
     * معادل دقیق Flutter getAllContacts()
     */
    suspend fun getAllContacts(limit: Int = 100, offset: Int = 0): List<Contact> = withContext(Dispatchers.IO) {
        try {
            return@withContext contactDao.getAllContacts(limit, offset)
        } catch (e: Exception) {
            println("خطا در دریافت تمام مخاطبین: $e")
            return@withContext emptyList()
        }
    }
    
    /**
     * اضافه کردن مخاطب جدید
     * معادل دقیق Flutter addContact()
     */
    suspend fun addContact(contact: Contact): Boolean = withContext(Dispatchers.IO) {
        try {
            val id = contactDao.insertContact(contact)
            clearCache() // پاک کردن کش بعد از تغییر
            return@withContext id > 0
        } catch (e: Exception) {
            println("خطا در اضافه کردن مخاطب: $e")
            return@withContext false
        }
    }
    
    /**
     * اضافه کردن چندین مخاطب به صورت bulk
     * معادل دقیق Flutter addContacts()
     */
    suspend fun addContacts(contacts: List<Contact>): Int = withContext(Dispatchers.IO) {
        try {
            val ids = contactDao.insertContacts(contacts)
            clearCache() // پاک کردن کش بعد از تغییر
            return@withContext ids.size
        } catch (e: Exception) {
            println("خطا در اضافه کردن مخاطبین: $e")
            return@withContext 0
        }
    }
    
    /**
     * اضافه کردن داده‌های نمونه
     * معادل دقیق Flutter addSampleData()
     */
    suspend fun addSampleData() = withContext(Dispatchers.IO) {
        try {
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
            clearCache()
            println("✅ ${sampleContacts.size} مخاطب نمونه اضافه شد")
        } catch (e: Exception) {
            println("❌ خطا در اضافه کردن داده‌های نمونه: $e")
        }
    }
    
    /**
     * دریافت آمار پایگاه داده
     * معادل دقیق Flutter getStatistics()
     */
    suspend fun getStatistics(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val stats = contactDao.getStatistics()
            return@withContext mapOf(
                "total" to stats.total,
                "withName" to stats.withName,
                "withMobile" to stats.withMobile,
                "withBirthDate" to stats.withBirthDate,
                "completenessPercentage" to if (stats.total > 0) (stats.withName * 100 / stats.total) else 0
            )
        } catch (e: Exception) {
            println("خطا در دریافت آمار: $e")
            return@withContext emptyMap()
        }
    }
    
    /**
     * پاک کردن کش و بارگیری مجدد داده‌ها
     * معادل دقیق Flutter clearCache()
     */
    fun clearCache() {
        cachedContacts = null
        lastCacheTime = 0
        println("✅ کش پاک شد")
    }
    
    /**
     * بارگیری مجدد کش به صورت دستی
     * معادل دقیق Flutter refreshCache()
     */
    suspend fun refreshCache() {
        clearCache()
        // بارگیری مجدد داده‌ها در پس‌زمینه
        getTotalCount()
    }
    
    /**
     * حذف تمام مخاطبین
     * معادل دقیق Flutter clearAllContacts()
     */
    suspend fun clearAllContacts() = withContext(Dispatchers.IO) {
        try {
            contactDao.deleteAllContacts()
            clearCache()
            println("✅ تمام مخاطبین حذف شدند")
        } catch (e: Exception) {
            println("❌ خطا در حذف مخاطبین: $e")
        }
    }
    
    /**
     * بررسی معتبر بودن کش
     */
    private fun isCacheValid(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastCacheTime) < cacheValidDuration
    }
}