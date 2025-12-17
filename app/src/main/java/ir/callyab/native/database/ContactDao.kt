package ir.callyab.native.database

import androidx.room.*
import androidx.lifecycle.LiveData
import ir.callyab.native.models.Contact

/**
 * DAO برای مخاطبین
 * معادل دقیق متدهای UniversalContactService Flutter
 */
@Dao
interface ContactDao {

    /** 
     * جستجو در مخاطبین بر اساس کد ملی 
     * معادل Flutter: searchContactByNationalCode()
     */
    @Query("SELECT * FROM contacts WHERE national_code = :nationalCode LIMIT 1")
    suspend fun findByNationalCode(nationalCode: String): Contact?

    /** 
     * جستجو در مخاطبین بر اساس شماره موبایل
     * معادل Flutter: searchContactByMobile()
     */
    @Query("SELECT * FROM contacts WHERE mobile = :mobile LIMIT 1")
    suspend fun findByMobile(mobile: String): Contact?

    /** 
     * جستجو در مخاطبین بر اساس شماره کارت
     * معادل Flutter: searchContactByCardNo()
     */
    @Query("SELECT * FROM contacts WHERE card_no = :cardNo LIMIT 1")
    suspend fun findByCardNo(cardNo: String): Contact?

    /** 
     * جستجوی عمومی در تمام فیلدها
     * معادل Flutter: searchContacts()
     */
    @Query("""
        SELECT * FROM contacts 
        WHERE national_code LIKE '%' || :query || '%' 
           OR card_no LIKE '%' || :query || '%'
           OR mobile LIKE '%' || :query || '%'
           OR full_name LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN national_code = :query THEN 1
                WHEN mobile = :query THEN 2
                WHEN card_no = :query THEN 3
                ELSE 4
            END,
            full_name ASC
        LIMIT :limit
    """)
    suspend fun searchContacts(query: String, limit: Int = 50): List<Contact>

    /** 
     * دریافت تعداد کل مخاطبین
     * معادل Flutter: getTotalCount()
     */
    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getTotalCount(): Int

    /** 
     * دریافت تمام مخاطبین با صفحه‌بندی
     * معادل Flutter: getAllContacts()
     */
    @Query("SELECT * FROM contacts ORDER BY full_name ASC LIMIT :limit OFFSET :offset")
    suspend fun getAllContacts(limit: Int = 100, offset: Int = 0): List<Contact>

    /** 
     * دریافت مخاطبین به صورت LiveData برای مشاهده تغییرات
     */
    @Query("SELECT * FROM contacts ORDER BY full_name ASC")
    fun getAllContactsLive(): LiveData<List<Contact>>

    /** 
     * اضافه کردن مخاطب جدید
     * معادل Flutter: addContact()
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    /** 
     * اضافه کردن چندین مخاطب به صورت bulk
     * معادل Flutter: addContacts()
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>): List<Long>

    /** 
     * به‌روزرسانی مخاطب
     */
    @Update
    suspend fun updateContact(contact: Contact)

    /** 
     * حذف مخاطب
     */
    @Delete
    suspend fun deleteContact(contact: Contact)

    /** 
     * حذف تمام مخاطبین
     * معادل Flutter: clearAllContacts()
     */
    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    /** 
     * آمار پایگاه داده
     * معادل Flutter: getStatistics()
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN full_name IS NOT NULL AND full_name != '' THEN 1 END) as withName,
            COUNT(CASE WHEN mobile IS NOT NULL AND mobile != '' THEN 1 END) as withMobile,
            COUNT(CASE WHEN birth_date IS NOT NULL AND birth_date != '' THEN 1 END) as withBirthDate
        FROM contacts
    """)
    suspend fun getStatistics(): DatabaseStatistics

    /**
     * بررسی وجود مخاطب با کد ملی
     */
    @Query("SELECT EXISTS(SELECT 1 FROM contacts WHERE national_code = :nationalCode)")
    suspend fun contactExists(nationalCode: String): Boolean
}

/**
 * کلاس کمکی برای آمار پایگاه داده
 */
data class DatabaseStatistics(
    val total: Int,
    val withName: Int,
    val withMobile: Int,
    val withBirthDate: Int
)