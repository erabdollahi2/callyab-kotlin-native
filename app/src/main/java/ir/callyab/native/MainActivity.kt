package ir.callyab.native

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.callyab.native.activities.LoginActivity
import ir.callyab.native.activities.DashboardActivity
import ir.callyab.native.services.AuthService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * MainActivity - صفحه اصلی و تعیین مسیر
 * معادل دقیق Flutter MainScreen + MyApp
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // مقداردهی سرویس احراز هویت
        authService = AuthService.getInstance(this)
        
        // تعیین صفحه اولیه
        determineInitialScreen()
    }
    
    /**
     * تعیین صفحه اولیه بر اساس وضعیت ورود کاربر
     * معادل دقیق Flutter MainScreen._determineInitialScreen()
     */
    private fun determineInitialScreen() {
        lifecycleScope.launch {
            try {
                // نمایش لوگو یا splash screen برای 1 ثانیه
                delay(1000)
                
                // بررسی ورود کاربر
                val intent = if (authService.isLoggedIn()) {
                    // کاربر وارد شده - برو به Dashboard
                    println("✅ کاربر وارد شده - انتقال به Dashboard")
                    Intent(this@MainActivity, DashboardActivity::class.java)
                } else {
                    // کاربر وارد نشده - برو به Login
                    println("🔐 کاربر وارد نشده - انتقال به Login")
                    Intent(this@MainActivity, LoginActivity::class.java)
                }
                
                startActivity(intent)
                finish() // بستن MainActivity
                
            } catch (e: Exception) {
                println("❌ خطا در تعیین صفحه اولیه: $e")
                // در صورت خطا، به صفحه ورود برو
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}