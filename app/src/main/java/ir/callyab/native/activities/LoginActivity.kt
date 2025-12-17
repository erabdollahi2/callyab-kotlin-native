package ir.callyab.native.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.callyab.native.services.AuthService
import kotlinx.coroutines.launch

/**
 * صفحه ورود و ثبت نام
 * معادل دقیق Flutter LoginScreen
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ایجاد UI به صورت Programmatic (معادل Flutter build method)
        createUI()
        
        // مقداردهی سرویس
        authService = AuthService.getInstance(this)
    }
    
    /**
     * ایجاد رابط کاربری
     * معادل دقیق Flutter LoginScreen.build()
     */
    private fun createUI() {
        // Layout اصلی
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 100)
        }
        
        // عنوان
        val titleText = TextView(this).apply {
            text = "ورود به کال یاب"
            textSize = 24f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 0, 0, 50)
        }
        
        // ایمیل
        val emailLabel = TextView(this).apply {
            text = "ایمیل:"
            textSize = 16f
            setPadding(0, 20, 0, 10)
        }
        
        emailEditText = EditText(this).apply {
            hint = "example@gmail.com"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(20, 20, 20, 20)
        }
        
        // رمز عبور
        val passwordLabel = TextView(this).apply {
            text = "رمز عبور:"
            textSize = 16f
            setPadding(0, 20, 0, 10)
        }
        
        passwordEditText = EditText(this).apply {
            hint = "رمز عبور"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(20, 20, 20, 20)
        }
        
        // دکمه ورود
        loginButton = Button(this).apply {
            text = "ورود / ثبت نام"
            textSize = 18f
            setPadding(0, 30, 0, 30)
            setOnClickListener { attemptLogin() }
        }
        
        // نوار پیشرفت
        progressBar = ProgressBar(this).apply {
            visibility = ProgressBar.GONE
        }
        
        // وضعیت
        statusTextView = TextView(this).apply {
            text = ""
            textSize = 14f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 0)
        }
        
        // راهنما
        val helpText = TextView(this).apply {
            text = """
                📱 کال یاب - تشخیص هویت تماس گیرنده
                
                • اگر حساب دارید، وارد شوید
                • اگر حساب ندارید، خودکار ثبت نام می‌شوید
                • ایمیل و رمز عبور را وارد کنید
                
                ورژن: Native Kotlin 1.0
            """.trimIndent()
            textSize = 12f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 30, 0, 0)
            alpha = 0.7f
        }
        
        // اضافه کردن همه view ها
        mainLayout.addView(titleText)
        mainLayout.addView(emailLabel)
        mainLayout.addView(emailEditText)
        mainLayout.addView(passwordLabel)
        mainLayout.addView(passwordEditText)
        mainLayout.addView(loginButton)
        mainLayout.addView(progressBar)
        mainLayout.addView(statusTextView)
        mainLayout.addView(helpText)
        
        setContentView(mainLayout)
    }
    
    /**
     * تلاش برای ورود
     * معادل دقیق Flutter LoginScreen._handleLogin()
     */
    private fun attemptLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        // اعتبارسنجی ورودی
        if (email.isEmpty()) {
            showStatus("لطفاً ایمیل را وارد کنید", false)
            return
        }
        
        if (password.isEmpty()) {
            showStatus("لطفاً رمز عبور را وارد کنید", false)
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showStatus("فرمت ایمیل صحیح نیست", false)
            return
        }
        
        // شروع پردازش
        setLoading(true)
        showStatus("در حال ورود...", true)
        
        lifecycleScope.launch {
            try {
                val user = authService.login(email, password)
                
                // ورود موفق
                showStatus("ورود موفق! خوش آمدید ${user.email}", true)
                
                // انتقال به Dashboard
                val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                // خطای ورود
                showStatus("خطا: ${e.message}", false)
                setLoading(false)
            }
        }
    }
    
    /**
     * نمایش وضعیت
     */
    private fun showStatus(message: String, isSuccess: Boolean) {
        statusTextView.text = message
        statusTextView.setTextColor(
            if (isSuccess) 
                android.graphics.Color.GREEN 
            else 
                android.graphics.Color.RED
        )
    }
    
    /**
     * تنظیم وضعیت بارگیری
     */
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) ProgressBar.VISIBLE else ProgressBar.GONE
        loginButton.isEnabled = !loading
        emailEditText.isEnabled = !loading
        passwordEditText.isEnabled = !loading
    }
}