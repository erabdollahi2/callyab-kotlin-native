package ir.callyab.native.database

import androidx.room.*
import ir.callyab.native.models.SearchRequest

/**
 * DAO برای درخواست‌های جستجو
 */
@Dao
interface SearchRequestDao {

    /** 
     * اضافه کردن درخواست جستجوی جدید
     */
    @Insert
    suspend fun insertSearchRequest(request: SearchRequest): Long

    /** 
     * دریافت تمام درخواست‌های کاربر
     */
    @Query("SELECT * FROM search_requests WHERE user_id = :userId ORDER BY requested_at DESC")
    suspend fun getUserSearchRequests(userId: String): List<SearchRequest>

    /** 
     * دریافت آخرین درخواست‌های کاربر
     */
    @Query("SELECT * FROM search_requests WHERE user_id = :userId ORDER BY requested_at DESC LIMIT :limit")
    suspend fun getRecentSearchRequests(userId: String, limit: Int = 10): List<SearchRequest>

    /** 
     * حذف درخواست‌های قدیمی (بیش از 30 روز)
     */
    @Query("DELETE FROM search_requests WHERE requested_at < :timestamp")
    suspend fun deleteOldRequests(timestamp: String)

    /** 
     * تعداد کل درخواست‌های کاربر
     */
    @Query("SELECT COUNT(*) FROM search_requests WHERE user_id = :userId")
    suspend fun getUserRequestCount(userId: String): Int
}