package ir.callyab.native.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.callyab.native.services.AuthService
import ir.callyab.native.services.UniversalContactService
import kotlinx.coroutines.launch

/**
 * صفحه اصلی Dashboard
 * معادل دقیق Flutter DashboardScreen
 */
class DashboardActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    private lateinit var contactService: UniversalContactService
    private lateinit var statsTextView: TextView
    private lateinit var menuLayout: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // مقداردهی سرویس‌ها
        authService = AuthService.getInstance(this)
        contactService = UniversalContactService.getInstance(this)
        
        // ایجاد UI
        createUI()
        
        // بارگیری آمار
        loadStatistics()
    }
    
    /**
     * ایجاد رابط کاربری Dashboard
     * معادل دقیق Flutter DashboardScreen.build()
     */
    private fun createUI() {
        // Layout اصلی
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
            text = "کال یاب - Dashboard"
            textSize = 22f
            weight = 1f
        }
        
        val logoutButton = Button(this).apply {
            text = "خروج"
            setOnClickListener { logout() }
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(logoutButton)
        
        // کاربر جاری
        val currentUser = authService.getCurrentUser()
        val userInfoText = TextView(this).apply {
            text = """
                👤 کاربر: ${currentUser?.email ?: "ناشناس"}
                💰 اعتبار: ${currentUser?.remainingUses ?: 0} استفاده
                🏷️ نقش: ${getRoleName(currentUser?.role)}
            """.trimIndent()
            textSize = 14f
            setPadding(0, 0, 0, 30)
        }
        
        // آمار سیستم
        statsTextView = TextView(this).apply {
            text = "در حال بارگیری آمار..."
            textSize = 16f
            setPadding(20, 20, 20, 20)
            background = getDrawable(android.R.drawable.editbox_background)
        }
        
        // منوی اصلی
        val menuTitle = TextView(this).apply {
            text = "📱 منوی اصلی:"
            textSize = 18f
            setPadding(0, 30, 0, 20)
        }
        
        menuLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // دکمه‌های منو
        createMenuButtons()
        
        // اضافه کردن همه view ها
        mainLayout.addView(headerLayout)
        mainLayout.addView(userInfoText)
        mainLayout.addView(statsTextView)
        mainLayout.addView(menuTitle)
        mainLayout.addView(menuLayout)
        
        setContentView(mainLayout)
    }
    
    /**
     * ایجاد دکمه‌های منو
     * معادل دقیق Flutter DashboardScreen menu items
     */
    private fun createMenuButtons() {
        // دکمه جستجو
        val lookupButton = Button(this).apply {
            text = "🔍 جستجوی مخاطبین"
            textSize = 16f
            setPadding(0, 15, 0, 15)
            setOnClickListener {
                val intent = Intent(this@DashboardActivity, LookupActivity::class.java)
                startActivity(intent)
            }
        }
        
        // دکمه شارژ
        val chargeButton = Button(this).apply {
            text = "💰 خرید اعتبار"
            textSize = 16f
            setPadding(0, 15, 0, 15)
            setOnClickListener {
                val intent = Intent(this@DashboardActivity, ChargeActivity::class.java)
                startActivity(intent)
            }
        }
        
        // دکمه حساب کاربری
        val accountButton = Button(this).apply {
            text = "👤 حساب کاربری"
            textSize = 16f
            setPadding(0, 15, 0, 15)
            setOnClickListener {
                val intent = Intent(this@DashboardActivity, AccountActivity::class.java)
                startActivity(intent)
            }
        }
        
        // دکمه پشتیبانی
        val supportButton = Button(this).apply {
            text = "🆘 پشتیبانی"
            textSize = 16f
            setPadding(0, 15, 0, 15)
            setOnClickListener {
                val intent = Intent(this@DashboardActivity, SupportActivity::class.java)
                startActivity(intent)
            }
        }
        
        menuLayout.addView(lookupButton)
        menuLayout.addView(chargeButton)
        menuLayout.addView(accountButton)
        menuLayout.addView(supportButton)
        
        // اگر ادمین است، دکمه ادمین اضافه کن
        if (authService.currentUserIsAdmin) {
            val adminButton = Button(this).apply {
                text = "⚙️ پنل ادمین"
                textSize = 16f
                setPadding(0, 15, 0, 15)
                setOnClickListener {
                    val intent = Intent(this@DashboardActivity, AdminPanelActivity::class.java)
                    startActivity(intent)
                }
            }
            menuLayout.addView(adminButton)
        }
    }
    
    /**
     * بارگیری آمار سیستم
     * معادل دقیق Flutter DashboardScreen.loadStatistics()
     */
    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                val count = contactService.getTotalCount()
                val stats = contactService.getStatistics()
                
                val statsText = """
                    📊 آمار پایگاه داده:
                    • تعداد کل مخاطبین: $count نفر
                    • مخاطبین با نام: ${stats["withName"]} نفر
                    • مخاطبین با موبایل: ${stats["withMobile"]} نفر
                    • درصد تکمیل: ${stats["completenessPercentage"]}%
                    
                    ✅ سیستم آماده است
                """.trimIndent()
                
                runOnUiThread {
                    statsTextView.text = statsText
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    statsTextView.text = "❌ خطا در بارگیری آمار: ${e.message}"
                }
            }
        }
    }
    
    /**
     * خروج از حساب کاربری
     * معادل دقیق Flutter logout
     */
    private fun logout() {
        authService.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    /**
     * تبدیل نقش کاربری به فارسی
     */
    private fun getRoleName(role: ir.callyab.native.services.UserRole?): String {
        return when (role) {
            ir.callyab.native.services.UserRole.USER -> "کاربر عادی"
            ir.callyab.native.services.UserRole.ADMIN -> "ادمین"
            ir.callyab.native.services.UserRole.SUPER_ADMIN -> "سوپر ادمین"
            null -> "نامشخص"
        }
    }
    
    override fun onResume() {
        super.onResume()
        // بروزرسانی آمار هنگام بازگشت
        loadStatistics()
    }
}