package ir.callyab.native.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ir.callyab.native.services.AuthService
import ir.callyab.native.models.PaymentPlan

/**
 * صفحه خرید اعتبار
 * معادل دقیق Flutter ChargeScreen
 */
class ChargeActivity : AppCompatActivity() {
    
    private lateinit var authService: AuthService
    private lateinit var balanceTextView: TextView
    private lateinit var planLayout: LinearLayout
    
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
            text = "💰 خرید اعتبار"
            textSize = 22f
            weight = 1f
        }
        
        val backButton = Button(this).apply {
            text = "بازگشت"
            setOnClickListener { finish() }
        }
        
        headerLayout.addView(titleText)
        headerLayout.addView(backButton)
        
        // موجودی فعلی
        val user = authService.getCurrentUser()
        balanceTextView = TextView(this).apply {
            text = """
                💰 موجودی فعلی: ${user?.remainingUses ?: 0} استفاده
                📊 کل استفاده‌ها: ${user?.usageCount ?: 0} بار
            """.trimIndent()
            textSize = 16f
            setPadding(20, 20, 20, 20)
            background = getDrawable(android.R.drawable.editbox_background)
        }
        
        // عنوان بسته‌ها
        val planTitle = TextView(this).apply {
            text = "📦 بسته‌های خرید:"
            textSize = 18f
            setPadding(0, 30, 0, 20)
        }
        
        planLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // ایجاد بسته‌های خرید
        createPaymentPlans()
        
        // اضافه کردن همه view ها
        mainLayout.addView(headerLayout)
        mainLayout.addView(balanceTextView)
        mainLayout.addView(planTitle)
        mainLayout.addView(planLayout)
        
        val scrollView = ScrollView(this)
        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    
    private fun createPaymentPlans() {
        PaymentPlan.paymentPlans.forEach { plan ->
            val planCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 20, 20, 20)
                background = getDrawable(android.R.drawable.editbox_background)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 15)
                layoutParams = params
            }
            
            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                weight = 1f
            }
            
            val planName = TextView(this).apply {
                text = plan.description
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            
            val planPrice = TextView(this).apply {
                text = "قیمت: ${plan.priceInToman.toString().reversed().chunked(3).joinToString(",").reversed()} تومان"
                textSize = 14f
                setPadding(0, 5, 0, 0)
            }
            
            val buyButton = Button(this).apply {
                text = "خرید"
                setOnClickListener { purchasePlan(plan) }
            }
            
            infoLayout.addView(planName)
            infoLayout.addView(planPrice)
            
            planCard.addView(infoLayout)
            planCard.addView(buyButton)
            
            planLayout.addView(planCard)
        }
    }
    
    private fun purchasePlan(plan: PaymentPlan) {
        Toast.makeText(this, "پرداخت برای ${plan.description} در دست توسعه است", Toast.LENGTH_LONG).show()
        // TODO: پیاده‌سازی درگاه پرداخت
    }
}