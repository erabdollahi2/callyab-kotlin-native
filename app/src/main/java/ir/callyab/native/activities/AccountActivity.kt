package ir.callyab.native.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.callyab.native.services.AuthService

/**
 * صفحه حساب کاربری
 * معادل دقیق Flutter AccountScreen
 */
class AccountActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authService = AuthService.getInstance(this)
        createUI()
    }
    
    private fun createUI() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 50, 30, 30)
        }
        
        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 30)
        }
        
        val titleText = TextView(this).apply {
            text = "👤 حساب کاربری"
            textSize = 22f
            weight = 1f
        }
        
        val backButton = Button(this).apply {
            text = "بازگشت"
            setOnClickListener { finish() }
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(backButton)
        
        // اطلاعات کاربر
        val user = authService.getCurrentUser()
        val userInfoText = TextView(this).apply {
            text = """
                📧 ایمیل: ${user?.email ?: "ناشناس"}
                💰 اعتبار باقی‌مانده: ${user?.remainingUses ?: 0} استفاده
                📊 کل استفاده‌ها: ${user?.usageCount ?: 0} بار
                🏷️ نقش: ${getRoleName(user?.role)}
                📅 آخرین ورود: ${user?.lastLogin ?: "ناشناس"}
                
                ✅ وضعیت: فعال
            """.trimIndent()
            textSize = 16f
            setPadding(20, 20, 20, 20)
            background = getDrawable(android.R.drawable.editbox_background)
        }
        
        // دکمه خروج
        val logoutButton = Button(this).apply {
            text = "🚪 خروج از حساب کاربری"
            textSize = 16f
            setPadding(0, 30, 0, 30)
            setOnClickListener { logout() }
        }
        
        // اضافه کردن همه view ها
        mainLayout.addView(headerLayout)
        mainLayout.addView(userInfoText)
        mainLayout.addView(logoutButton)
        
        setContentView(mainLayout)
    }
    
    private fun getRoleName(role: ir.callyab.native.services.UserRole?): String {
        return when (role) {
            ir.callyab.native.services.UserRole.USER -> "کاربر عادی"
            ir.callyab.native.services.UserRole.ADMIN -> "ادمین"
            ir.callyab.native.services.UserRole.SUPER_ADMIN -> "سوپر ادمین"
            null -> "نامشخص"
        }
    }
    
    private fun logout() {
        authService.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}