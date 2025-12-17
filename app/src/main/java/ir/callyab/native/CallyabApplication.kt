package ir.callyab.native

import android.app.Application
import ir.callyab.native.database.CallyabDatabase
import ir.callyab.native.database.DatabaseInitializer
import ir.callyab.native.services.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application کلاس اصلی
 * معادل دقیق Flutter main() function
 */
class CallyabApplication : Application() {

    // Services
    private lateinit var authService: AuthService
    private lateinit var universalContactService: UniversalContactService
    private lateinit var callerIdService: CallerIdService

    override fun onCreate() {
        super.onCreate()
        
        println("🚀 === شروع برنامه CallyAB Native ===")
        
        // مقداردهی سرویس‌ها
        initializeServices()
        
        // مقداردهی پایگاه داده
        initializeDatabase()
    }
    
    /**
     * مقداردهی سرویس‌ها
     * معادل دقیق Flutter main() services initialization
     */
    private fun initializeServices() {
        try {
            // Auth Service
            authService = AuthService.getInstance(this)
            
            // Universal Contact Service
            universalContactService = UniversalContactService.getInstance(this)
            
            // Caller ID Service
            callerIdService = CallerIdService.getInstance(this)
            
            println("✅ سرویس‌ها مقداردهی شدند")
        } catch (e: Exception) {
            println("❌ خطا در مقداردهی سرویس‌ها: $e")
        }
    }
    
    /**
     * مقداردهی پایگاه داده SQLite
     * معادل دقیق Flutter DatabaseInitializer.initializeDatabase()
     */
    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("🔄 در حال مقداردهی پایگاه داده SQLite...")
                
                // مقداردهی پایگاه داده
                DatabaseInitializer.initializeWithSampleData(this@CallyabApplication)
                
                // بررسی تعداد رکوردها
                val count = universalContactService.getTotalCount()
                
                if (count == 0) {
                    println("📝 پایگاه داده خالی است - بارگیری داده‌های نمونه...")
                    universalContactService.addSampleData()
                } else {
                    println("✅ پایگاه داده آماده - تعداد رکوردها: $count")
                }
                
                println("✅ پایگاه داده SQLite آماده است")
                
                // نمایش آمار در حالت دیباگ
                showDatabaseStats()
                
            } catch (e: Exception) {
                println("❌ خطا در مقداردهی پایگاه داده: $e")
            }
        }
    }
    
    /**
     * نمایش آمار پایگاه داده
     * معادل دقیق Flutter main() debug statistics
     */
    private suspend fun showDatabaseStats() {
        try {
            val count = universalContactService.getTotalCount()
            println("📊 تعداد کل مخاطبین: $count")
            
            val info = DatabaseInitializer.getDatabaseInfo(this)
            println("📄 آمار پایگاه داده: $info")
            
        } catch (e: Exception) {
            println("❌ خطا در دریافت آمار: $e")
        }
    }
    
    /**
     * شروع Caller ID Service در صورت فعال بودن
     * معادل دقیق Flutter main() CallerIdService start
     */
    private suspend fun startCallerIdIfEnabled() {
        try {
            val settings = callerIdService.getServiceSettings()
            
            if (settings["enabled"] == true) {
                println("🔄 شروع Caller ID Service...")
                val started = callerIdService.startCallerIdService()
                if (started) {
                    println("✅ Caller ID Service آماده است")
                } else {
                    println("❌ خطا در شروع Caller ID Service")
                }
            }
        } catch (e: Exception) {
            println("❌ خطا در شروع Caller ID: $e")
        }
    }
}