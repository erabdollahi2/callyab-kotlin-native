package ir.callyab.native.database

import androidx.room.*
import ir.callyab.native.models.PaymentTransaction

/**
 * DAO برای تراکنش‌های پرداخت
 */
@Dao
interface PaymentTransactionDao {

    /** 
     * اضافه کردن تراکنش جدید
     */
    @Insert
    suspend fun insertTransaction(transaction: PaymentTransaction): Long

    /** 
     * دریافت تمام تراکنش‌های کاربر
     */
    @Query("SELECT * FROM payment_transactions WHERE user_id = :userId ORDER BY timestamp DESC")
    suspend fun getUserTransactions(userId: String): List<PaymentTransaction>

    /** 
     * دریافت تراکنش‌های موفق کاربر
     */
    @Query("SELECT * FROM payment_transactions WHERE user_id = :userId AND success = 1 ORDER BY timestamp DESC")
    suspend fun getSuccessfulTransactions(userId: String): List<PaymentTransaction>

    /** 
     * محاسبه کل استفاده‌های خریداری شده
     */
    @Query("SELECT COALESCE(SUM(uses_added), 0) FROM payment_transactions WHERE user_id = :userId AND success = 1")
    suspend fun getTotalPurchasedUses(userId: String): Int

    /** 
     * محاسبه کل مبلغ پرداختی
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM payment_transactions WHERE user_id = :userId AND success = 1")
    suspend fun getTotalSpentAmount(userId: String): Int
}