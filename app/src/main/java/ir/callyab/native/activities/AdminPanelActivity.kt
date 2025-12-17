package ir.callyab.native.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.callyab.native.services.AuthService
import ir.callyab.native.services.UniversalContactService
import kotlinx.coroutines.launch

/**
 * صفحه پنل ادمین
 * معادل دقیق Flutter AdminPanelScreen
 */
class AdminPanelActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    private lateinit var contactService: UniversalContactService
    private lateinit var statsTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // بررسی دسترسی ادمین
        authService = AuthService.getInstance(this)
        if (!authService.currentUserIsAdmin) {
            Toast.makeText(this, "دسترسی غیرمجاز", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        contactService = UniversalContactService.getInstance(this)
        createUI()
        loadAdminStats()
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
            text = "⚙️ پنل ادمین"
            textSize = 22f
            weight = 1f
        }
        
        val backButton = Button(this).apply {
            text = "بازگشت"
            setOnClickListener { finish() }
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(backButton)
        
        // آمار ادمین
        statsTextView = TextView(this).apply {
            text = "در حال بارگیری آمار..."
            textSize = 14f
            setPadding(20, 20, 20, 20)
            background = getDrawable(android.R.drawable.editbox_background)
        }
        
        // دکمه‌های ادمین
        val adminTitle = TextView(this).apply {
            text = "🛠️ ابزارهای مدیریت:"
            textSize = 18f
            setPadding(0, 30, 0, 20)
        }
        
        val refreshDataButton = Button(this).apply {
            text = "🔄 بروزرسانی داده‌ها"
            textSize = 16f
            setPadding(0, 15, 0, 15)
            setOnClickListener { refreshData() }
        }
        
        val clearCacheButton = Button(this).apply {
            text = "🗑️ پاک کردن کش"
            textSize = 16f
            setPadding(0, 15, 0, 15)
            setOnClickListener { clearCache() }
        }
        
        val addSampleDataButton = Button(this).apply {
            text = "📝 اضافه کردن داده نمونه"
            textSize = 16f
            setPadding(0, 15, 0, 15)
            setOnClickListener { addSampleData() }
        }
        
        // اضافه کردن همه view ها
        mainLayout.addView(headerLayout)
        mainLayout.addView(statsTextView)
        mainLayout.addView(adminTitle)
        mainLayout.addView(refreshDataButton)
        mainLayout.addView(clearCacheButton)
        mainLayout.addView(addSampleDataButton)
        
        val scrollView = ScrollView(this)
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    private fun loadAdminStats() {
        lifecycleScope.launch {
            try {
                val count = contactService.getTotalCount()
                val stats = contactService.getStatistics()
                
                val statsText = """
                    👑 آمار پنل ادمین:
                    
                    📊 پایگاه داده:
                    • تعداد کل مخاطبین: $count نفر
                    • مخاطبین با نام: ${stats["withName"]} نفر
                    • مخاطبین با موبایل: ${stats["withMobile"]} نفر
                    • مخاطبین با تاریخ تولد: ${stats["withBirthDate"]} نفر
                    • درصد تکمیل: ${stats["completenessPercentage"]}%
                    
                    👤 کاربر فعلی: ${authService.getCurrentUser()?.email}
                    🏷️ نقش: ${if (authService.currentUserIsSuperAdmin) "سوپر ادمین" else "ادمین"}
                    
                    ✅ سیستم در حال اجرا
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
    
    private fun refreshData() {
        lifecycleScope.launch {
            try {
                contactService.refreshCache()
                Toast.makeText(this@AdminPanelActivity, "✅ داده‌ها بروزرسانی شد", Toast.LENGTH_SHORT).show()
                loadAdminStats()
            } catch (e: Exception) {
                Toast.makeText(this@AdminPanelActivity, "❌ خطا در بروزرسانی: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun clearCache() {
        contactService.clearCache()
        Toast.makeText(this, "✅ کش پاک شد", Toast.LENGTH_SHORT).show()
        loadAdminStats()
    }
    
    private fun addSampleData() {
        lifecycleScope.launch {
            try {
                contactService.addSampleData()
                Toast.makeText(this@AdminPanelActivity, "✅ داده‌های نمونه اضافه شد", Toast.LENGTH_SHORT).show()
                loadAdminStats()
            } catch (e: Exception) {
                Toast.makeText(this@AdminPanelActivity, "❌ خطا در اضافه کردن داده: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}