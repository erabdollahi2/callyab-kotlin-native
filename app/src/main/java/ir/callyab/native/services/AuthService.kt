package ir.callyab.native.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import android.util.Patterns
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * مدل کاربر - معادل دقیق Flutter User class
 */
data class User(
    val email: String,
    val password: String,
    var balance: Int = 0,
    val role: UserRole = UserRole.USER,
    var lastLogin: String = Date().time.toString(),
    var usageCount: Int = 0,
    var remainingUses: Int = 0
) {
    companion object {
        /** سوپر ادمین‌ها - معادل Flutter User.superAdminEmails */
        val superAdminEmails = listOf(
            "admin@callyab.ir",
            "support@callyab.ir",
            "erabdollahi2@gmail.com"
        )
    }
}

/**
 * نقش‌های کاربری - معادل دقیق Flutter UserRole
 */
enum class UserRole {
    USER,
    ADMIN, 
    SUPER_ADMIN
}

/**
 * سرویس مدیریت کاربران و احراز هویت مبتنی بر OTP
 * معادل دقیق Flutter AuthService
 */
class AuthService private constructor(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("callyab_auth", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val USERS_KEY = "users"
        private const val SESSION_KEY = "current_user_phone"
        
        @Volatile
        private var INSTANCE: AuthService? = null
        
        fun getInstance(context: Context): AuthService {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * ورود/ثبت‌نام با ایمیل و رمز عبور
     * معادل دقیق Flutter login()
     */
    suspend fun login(email: String, password: String): User = withContext(Dispatchers.IO) {
        // نرمال‌سازی ایمیل
        val normalizedEmail = email.trim().lowercase()
        
        // بررسی وجود کاربر
        val existingUser = getUserByEmail(normalizedEmail)
        
        if (existingUser != null) {
            // کاربر موجود - بررسی رمز عبور
            if (existingUser.password == password) {
                // ورود موفق - بروزرسانی آخرین ورود
                val updatedUser = existingUser.copy(lastLogin = Date().time.toString())
                saveUser(updatedUser)
                prefs.edit().putString(SESSION_KEY, normalizedEmail).apply()
                return@withContext updatedUser
            } else {
                throw Exception("رمز عبور اشتباه است")
            }
        } else {
            // کاربر جدید - ثبت‌نام خودکار
            val newUser = User(
                email = normalizedEmail,
                password = password,
                balance = 0,
                role = determineUserRole(normalizedEmail)
            )
            
            saveUser(newUser)
            prefs.edit().putString(SESSION_KEY, normalizedEmail).apply()
            return@withContext newUser
        }
    }
    
    /**
     * بررسی وضعیت ورود کاربر
     * معادل دقیق Flutter isLoggedIn()
     */
    fun isLoggedIn(): Boolean {
        val email = prefs.getString(SESSION_KEY, null)
        return !email.isNullOrEmpty() && getUserByEmail(email) != null
    }
    
    /**
     * دریافت کاربر جاری
     * معادل دقیق Flutter getCurrentUser()
     */
    fun getCurrentUser(): User? {
        val email = prefs.getString(SESSION_KEY, null) ?: return null
        return getUserByEmail(email)
    }
    
    /**
     * خروج از حساب کاربری
     * معادل دقیق Flutter logout()
     */
    fun logout() {
        prefs.edit().remove(SESSION_KEY).apply()
        println("✅ کاربر خارج شد")
    }
    
    /**
     * بررسی ادمین بودن کاربر جاری
     * معادل دقیق Flutter currentUserIsAdmin
     */
    val currentUserIsAdmin: Boolean
        get() {
            val user = getCurrentUser()
            return user?.role == UserRole.ADMIN || user?.role == UserRole.SUPER_ADMIN
        }
    
    /**
     * بررسی سوپرادمین بودن کاربر جاری
     * معادل دقیق Flutter currentUserIsSuperAdmin
     */
    val currentUserIsSuperAdmin: Boolean
        get() = getCurrentUser()?.role == UserRole.SUPER_ADMIN
    
    /**
     * بررسی ضرورت درخواست مجوز مخاطبین
     * معادل دقیق Flutter shouldRequestContactsPermission()
     */
    suspend fun shouldRequestContactsPermission(): Boolean {
        // اگر کاربر وارد نشده، نیازی نیست
        if (!isLoggedIn()) return false
        
        // TODO: پیاده‌سازی بررسی import مخاطبین قبلی
        // فعلاً true برمی‌گردانیم تا همیشه بپرسد
        return true
    }
    
    /**
     * افزایش اعتبار کاربر
     * معادل دقیق Flutter addBalance()
     */
    suspend fun addBalance(amount: Int, usesAdded: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = getCurrentUser() ?: return@withContext false
            val updatedUser = user.copy(
                balance = user.balance + amount,
                remainingUses = user.remainingUses + usesAdded
            )
            saveUser(updatedUser)
            return@withContext true
        } catch (e: Exception) {
            println("خطا در افزایش اعتبار: $e")
            return@withContext false
        }
    }
    
    /**
     * کاهش تعداد استفاده‌های باقیمانده
     * معادل دقیق Flutter decrementUsage()
     */
    suspend fun decrementUsage(): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = getCurrentUser() ?: return@withContext false
            if (user.remainingUses <= 0) return@withContext false
            
            val updatedUser = user.copy(
                remainingUses = user.remainingUses - 1,
                usageCount = user.usageCount + 1
            )
            saveUser(updatedUser)
            return@withContext true
        } catch (e: Exception) {
            println("خطا در کاهش استفاده: $e")
            return@withContext false
        }
    }
    
    /**
     * دریافت کاربر بر اساس ایمیل
     * معادل دقیق Flutter _getUserByEmail()
     */
    private fun getUserByEmail(email: String): User? {
        val allUsers = getAllUsers()
        return allUsers.find { it.email == email }
    }
    
    /**
     * ذخیره کاربر
     * معادل دقیق Flutter _saveUser()
     */
    private fun saveUser(user: User) {
        val allUsers = getAllUsers().toMutableList()
        val index = allUsers.indexOfFirst { it.email == user.email }
        
        if (index >= 0) {
            allUsers[index] = user
        } else {
            allUsers.add(user)
        }
        
        val usersJson = gson.toJson(allUsers)
        prefs.edit().putString(USERS_KEY, usersJson).apply()
    }
    
    /**
     * دریافت تمام کاربران
     * معادل دقیق Flutter _getAllUsers()
     */
    private fun getAllUsers(): List<User> {
        val usersJson = prefs.getString(USERS_KEY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(usersJson, type) ?: emptyList()
        } catch (e: Exception) {
            println("خطا در خواندن کاربران: $e")
            emptyList()
        }
    }
    
    /**
     * تعیین نقش کاربر بر اساس ایمیل
     * معادل دقیق Flutter _determineUserRole()
     */
    private fun determineUserRole(email: String): UserRole {
        return when {
            User.superAdminEmails.contains(email) -> UserRole.SUPER_ADMIN
            getAllUsers().isEmpty() -> UserRole.ADMIN // اولین کاربر
            else -> UserRole.USER
        }
    }
    
    /**
     * اعتبارسنجی فرمت ایمیل
     * معادل دقیق Flutter _isValidEmail()
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}