package ir.callyab.native.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * صفحه پشتیبانی و ارتباط با ادمین
 * معادل دقیق Flutter SupportScreen
 */
class SupportActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            text = "🆘 پشتیبانی"
            textSize = 22f
            weight = 1f
        }
        
        val backButton = Button(this).apply {
            text = "بازگشت"
            setOnClickListener { finish() }
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(backButton)
        
        // اطلاعات تماس
        val contactInfoText = TextView(this).apply {
            text = """
                📞 راه‌های ارتباط با پشتیبانی:
                
                📧 ایمیل: support@callyab.ir
                📱 تلگرام: @callyab_support
                ⏰ ساعات کاری: 9 صبح تا 18 عصر
                
                💡 سوالات متداول:
                
                ❓ چگونه اعتبار خریداری کنم؟
                از منوی "خرید اعتبار" استفاده کنید
                
                ❓ چرا نتیجه جستجو پیدا نمی‌شود؟
                ممکن است اطلاعات در پایگاه داده موجود نباشد
                
                ❓ چگونه تشخیص تماس فعال کنم؟
                از تنظیمات اپ، گزینه Caller ID را فعال کنید
                
                🔧 نسخه: Native Kotlin 1.0
                🏢 سازنده: تیم توسعه کال یاب
            """.trimIndent()
            textSize = 14f
            setPadding(20, 20, 20, 20)
            background = getDrawable(android.R.drawable.editbox_background)
        }
        
        // فرم ارسال پیام
        val messageTitle = TextView(this).apply {
            text = "💬 ارسال پیام به پشتیبانی:"
            textSize = 16f
            setPadding(0, 30, 0, 15)
        }
        
        val messageEditText = EditText(this).apply {
            hint = "پیام خود را اینجا بنویسید..."
            minLines = 3
            maxLines = 6
            setPadding(20, 20, 20, 20)
            background = getDrawable(android.R.drawable.editbox_background)
        }
        
        val sendButton = Button(this).apply {
            text = "📤 ارسال پیام"
            textSize = 16f
            setPadding(0, 20, 0, 20)
            setOnClickListener {
                val message = messageEditText.text.toString().trim()
                if (message.isNotEmpty()) {
                    Toast.makeText(this@SupportActivity, "پیام شما ارسال شد. به زودی پاسخ خواهید گرفت", Toast.LENGTH_LONG).show()
                    messageEditText.text.clear()
                } else {
                    Toast.makeText(this@SupportActivity, "لطفاً پیام خود را وارد کنید", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // اضافه کردن همه view ها
        mainLayout.addView(headerLayout)
        mainLayout.addView(contactInfoText)
        mainLayout.addView(messageTitle)
        mainLayout.addView(messageEditText)
        mainLayout.addView(sendButton)
        
        val scrollView = ScrollView(this)
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
}