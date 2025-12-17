package ir.callyab.native.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.callyab.native.services.AuthService
import ir.callyab.native.services.UniversalContactService
import ir.callyab.native.models.Contact
import kotlinx.coroutines.launch

/**
 * صفحه جستجوی مخاطبین
 * معادل دقیق Flutter LookupScreen
 */
class LookupActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    private lateinit var contactService: UniversalContactService
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resultsLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var statusTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // مقداردهی سرویس‌ها
        authService = AuthService.getInstance(this)
        contactService = UniversalContactService.getInstance(this)
        
        // ایجاد UI
        createUI()
    }
    
    /**
     * ایجاد رابط کاربری جستجو
     * معادل دقیق Flutter LookupScreen.build()
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
            text = "🔍 جستجوی مخاطبین"
            textSize = 22f
            weight = 1f
        }
        
        val backButton = Button(this).apply {
            text = "بازگشت"
            setOnClickListener { finish() }
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(backButton)
        
        // راهنمای جستجو
        val instructionText = TextView(this).apply {
            text = """
                💡 راهنمای جستجو:
                • کد ملی (10 رقم)
                • شماره موبایل (11 رقم)
                • شماره کارت (16 رقم)
                • نام و نام خانوادگی
            """.trimIndent()
            textSize = 14f
            setPadding(0, 0, 0, 20)
            alpha = 0.8f
        }
        
        // فیلد جستجو
        val searchLabel = TextView(this).apply {
            text = "جستجو:"
            textSize = 16f
            setPadding(0, 0, 0, 10)
        }
        
        searchEditText = EditText(this).apply {
            hint = "کد ملی، شماره موبایل، یا نام"
            setPadding(20, 20, 20, 20)
            textSize = 16f
        }
        
        // دکمه جستجو
        searchButton = Button(this).apply {
            text = "🔍 جستجو"
            textSize = 18f
            setPadding(0, 20, 0, 20)
            setOnClickListener { performSearch() }
        }
        
        // نوار پیشرفت
        progressBar = ProgressBar(this).apply {
            visibility = ProgressBar.GONE
        }
        
        // وضعیت جستجو
        statusTextView = TextView(this).apply {
            text = ""
            textSize = 14f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 20)
        }
        
        // نتایج جستجو
        val resultsTitle = TextView(this).apply {
            text = "نتایج جستجو:"
            textSize = 18f
            setPadding(0, 20, 0, 10)
        }
        
        resultsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // اضافه کردن همه view ها
        mainLayout.addView(headerLayout)
        mainLayout.addView(instructionText)
        mainLayout.addView(searchLabel)
        mainLayout.addView(searchEditText)
        mainLayout.addView(searchButton)
        mainLayout.addView(progressBar)
        mainLayout.addView(statusTextView)
        mainLayout.addView(resultsTitle)
        mainLayout.addView(resultsLayout)
        
        // قابلیت اسکرول
        val scrollView = ScrollView(this)
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    /**
     * انجام جستجو
     * معادل دقیق Flutter LookupScreen._handleSearch()
     */
    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        
        if (query.isEmpty()) {
            showStatus("لطفاً عبارت جستجو را وارد کنید", false)
            return
        }
        
        if (query.length < 3) {
            showStatus("حداقل 3 کاراکتر وارد کنید", false)
            return
        }
        
        // بررسی اعتبار کاربر
        val user = authService.getCurrentUser()
        if (user?.remainingUses ?: 0 <= 0) {
            showStatus("❌ اعتبار شما تمام شده است. لطفاً شارژ کنید", false)
            return
        }
        
        // شروع جستجو
        setSearching(true)
        showStatus("در حال جستجو...", true)
        clearResults()
        
        lifecycleScope.launch {
            try {
                // انواع مختلف جستجو
                val results = mutableListOf<Contact>()
                
                // 1. جستجوی کد ملی
                if (query.length == 10 && query.all { it.isDigit() }) {
                    contactService.searchContactByNationalCode(query)?.let { 
                        results.add(it) 
                    }
                }
                
                // 2. جستجوی شماره موبایل
                if (query.length == 11 && query.startsWith("09")) {
                    contactService.searchContactByMobile(query)?.let { 
                        results.add(it) 
                    }
                }
                
                // 3. جستجوی شماره کارت
                if (query.length == 16 && query.all { it.isDigit() }) {
                    contactService.searchContactByCardNo(query)?.let { 
                        results.add(it) 
                    }
                }
                
                // 4. جستجوی عمومی
                if (results.isEmpty()) {
                    val generalResults = contactService.searchContacts(query, 20)
                    results.addAll(generalResults)
                }
                
                runOnUiThread {
                    setSearching(false)
                    
                    if (results.isNotEmpty()) {
                        // کاهش اعتبار کاربر
                        lifecycleScope.launch {
                            authService.decrementUsage()
                        }
                        
                        showStatus("✅ ${results.size} نتیجه پیدا شد", true)
                        displayResults(results)
                    } else {
                        showStatus("❌ نتیجه‌ای پیدا نشد", false)
                    }
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    setSearching(false)
                    showStatus("❌ خطا در جستجو: ${e.message}", false)
                }
            }
        }
    }
    
    /**
     * نمایش نتایج جستجو
     * معادل دقیق Flutter LookupScreen.displayResults()
     */
    private fun displayResults(contacts: List<Contact>) {
        contacts.forEach { contact ->
            val contactCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20, 20, 20, 20)
                background = getDrawable(android.R.drawable.editbox_background)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 20)
                layoutParams = params
            }
            
            // نام
            val nameText = TextView(this).apply {
                text = "👤 نام: ${contact.fullName ?: "نامشخص"}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 10)
            }
            
            // کد ملی
            val nationalCodeText = TextView(this).apply {
                text = "🆔 کد ملی: ${contact.nationalCode}"
                textSize = 14f
                setPadding(0, 0, 0, 5)
            }
            
            // شماره موبایل
            val mobileText = TextView(this).apply {
                text = "📱 موبایل: ${contact.mobile ?: "نامشخص"}"
                textSize = 14f
                setPadding(0, 0, 0, 5)
            }
            
            // شماره کارت
            val cardText = TextView(this).apply {
                text = "💳 کارت: ${contact.cardNo}"
                textSize = 14f
                setPadding(0, 0, 0, 5)
            }
            
            // تاریخ تولد
            val birthDateText = TextView(this).apply {
                text = "🎂 تولد: ${contact.birthDate ?: "نامشخص"}"
                textSize = 14f
            }
            
            contactCard.addView(nameText)
            contactCard.addView(nationalCodeText)
            contactCard.addView(mobileText)
            contactCard.addView(cardText)
            contactCard.addView(birthDateText)
            
            resultsLayout.addView(contactCard)
        }
    }
    
    /**
     * پاک کردن نتایج قبلی
     */
    private fun clearResults() {
        resultsLayout.removeAllViews()
    }
    
    /**
     * نمایش وضعیت
     */
    private fun showStatus(message: String, isSuccess: Boolean) {
        statusTextView.text = message
        statusTextView.setTextColor(
            if (isSuccess) 
                android.graphics.Color.parseColor("#4CAF50") 
            else 
                android.graphics.Color.parseColor("#F44336")
        )
    }
    
    /**
     * تنظیم وضعیت جستجو
     */
    private fun setSearching(searching: Boolean) {
        progressBar.visibility = if (searching) ProgressBar.VISIBLE else ProgressBar.GONE
        searchButton.isEnabled = !searching
        searchEditText.isEnabled = !searching
    }
}