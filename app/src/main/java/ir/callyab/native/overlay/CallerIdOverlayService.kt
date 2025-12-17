package ir.callyab.native.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import ir.callyab.native.models.Contact
import ir.callyab.native.utils.Constants
import ir.callyab.native.utils.Helpers

/**
 * سرویس نمایش overlay برای caller ID
 * معادل دقیق Flutter caller_id_overlay.dart + overlay_entry.dart
 */
class CallerIdOverlayService : Service() {
    
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isOverlayShowing = false
    
    companion object {
        private const val OVERLAY_TAG = "CallerIdOverlay"
        
        fun showCallerInfo(context: Context, contact: Contact) {
            val intent = Intent(context, CallerIdOverlayService::class.java).apply {
                action = "SHOW_CALLER_INFO"
                putExtra("contact", contact)
            }
            context.startService(intent)
        }
        
        fun showUnknownCaller(context: Context, phoneNumber: String) {
            val intent = Intent(context, CallerIdOverlayService::class.java).apply {
                action = "SHOW_UNKNOWN_CALLER"
                putExtra("phone_number", phoneNumber)
            }
            context.startService(intent)
        }
        
        fun hideOverlay(context: Context) {
            val intent = Intent(context, CallerIdOverlayService::class.java).apply {
                action = "HIDE_OVERLAY"
            }
            context.startService(intent)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "SHOW_CALLER_INFO" -> {
                val contact = intent.getParcelableExtra<Contact>("contact")
                contact?.let { showCallerInfoOverlay(it) }
            }
            "SHOW_UNKNOWN_CALLER" -> {
                val phoneNumber = intent.getStringExtra("phone_number") ?: ""
                showUnknownCallerOverlay(phoneNumber)
            }
            "HIDE_OVERLAY" -> {
                hideOverlay()
            }
        }
        return START_NOT_STICKY
    }
    
    /**
     * نمایش overlay برای مخاطب شناخته شده
     * معادل دقیق Flutter _showCallerIdOverlay()
     */
    @SuppressLint("InflateParams")
    private fun showCallerInfoOverlay(contact: Contact) {
        if (isOverlayShowing) {
            hideOverlay()
        }
        
        try {
            // ایجاد layout overlay
            overlayView = LayoutInflater.from(this).inflate(
                createCallerInfoLayout(contact), 
                null
            )
            
            // تنظیمات window
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 100 // فاصله از بالای صفحه
            }
            
            // اضافه کردن overlay
            windowManager?.addView(overlayView, layoutParams)
            isOverlayShowing = true
            
            // انیمیشن ورود
            showEnterAnimation()
            
            // زمان‌بندی حذف خودکار
            scheduleAutoHide()
            
            println("🔔 Overlay نمایش داده شد برای: ${contact.fullName}")
            
        } catch (e: Exception) {
            println("❌ خطا در نمایش overlay: $e")
        }
    }
    
    /**
     * نمایش overlay برای تماس ناشناس
     * معادل دقیق Flutter _showUnknownCallerOverlay()
     */
    @SuppressLint("InflateParams")
    private fun showUnknownCallerOverlay(phoneNumber: String) {
        if (isOverlayShowing) {
            hideOverlay()
        }
        
        try {
            overlayView = LayoutInflater.from(this).inflate(
                createUnknownCallerLayout(phoneNumber), 
                null
            )
            
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 100
            }
            
            windowManager?.addView(overlayView, layoutParams)
            isOverlayShowing = true
            
            showEnterAnimation()
            scheduleAutoHide()
            
            println("❓ Overlay نمایش داده شد برای شماره ناشناس: $phoneNumber")
            
        } catch (e: Exception) {
            println("❌ خطا در نمایش overlay ناشناس: $e")
        }
    }
    
    /**
     * ایجاد layout برای مخاطب شناخته شده
     */
    private fun createCallerInfoLayout(contact: Contact): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@CallerIdOverlayService, android.R.drawable.dialog_holo_light_frame)
            setPadding(40, 30, 40, 30)
            elevation = 10f
            
            // آیکون
            val iconImageView = ImageView(this@CallerIdOverlayService).apply {
                setImageResource(android.R.drawable.sym_call_incoming)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 20)
                }
            }
            addView(iconImageView)
            
            // نام
            val nameTextView = TextView(this@CallerIdOverlayService).apply {
                text = contact.fullName ?: "نام نامشخص"
                textSize = 22f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 10)
                }
            }
            addView(nameTextView)
            
            // شماره موبایل
            val phoneTextView = TextView(this@CallerIdOverlayService).apply {
                text = contact.mobile ?: "شماره نامشخص"
                textSize = 16f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 5)
                }
            }
            addView(phoneTextView)
            
            // کد ملی (اختیاری)
            if (contact.nationalCode.isNotEmpty()) {
                val nationalCodeTextView = TextView(this@CallerIdOverlayService).apply {
                    text = "کد ملی: ${contact.nationalCode}"
                    textSize = 14f
                    setTextColor(Color.GRAY)
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        setMargins(0, 0, 0, 5)
                    }
                }
                addView(nationalCodeTextView)
            }
            
            // برچسب
            val labelTextView = TextView(this@CallerIdOverlayService).apply {
                text = "🔍 کال یاب"
                textSize = 12f
                setTextColor(Color.BLUE)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 15, 0, 0)
                }
            }
            addView(labelTextView)
            
            // دکمه بستن
            val closeButton = Button(this@CallerIdOverlayService).apply {
                text = "✕"
                textSize = 16f
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.RED)
                layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 10, 0, 0)
                }
                setOnClickListener { hideOverlay() }
            }
            addView(closeButton)
        }
    }
    
    /**
     * ایجاد layout برای تماس ناشناس
     */
    private fun createUnknownCallerLayout(phoneNumber: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@CallerIdOverlayService, android.R.drawable.dialog_holo_light_frame)
            setPadding(40, 30, 40, 30)
            elevation = 10f
            
            // آیکون سوال
            val iconImageView = ImageView(this@CallerIdOverlayService).apply {
                setImageResource(android.R.drawable.ic_dialog_info)
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 20)
                }
            }
            addView(iconImageView)
            
            // عنوان
            val titleTextView = TextView(this@CallerIdOverlayService).apply {
                text = "تماس ناشناس"
                textSize = 20f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 10)
                }
            }
            addView(titleTextView)
            
            // شماره
            val phoneTextView = TextView(this@CallerIdOverlayService).apply {
                text = phoneNumber
                textSize = 16f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 10)
                }
            }
            addView(phoneTextView)
            
            // پیام
            val messageTextView = TextView(this@CallerIdOverlayService).apply {
                text = "این شماره در پایگاه داده یافت نشد"
                textSize = 14f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, 15)
                }
            }
            addView(messageTextView)
            
            // برچسب
            val labelTextView = TextView(this@CallerIdOverlayService).apply {
                text = "🔍 کال یاب"
                textSize = 12f
                setTextColor(Color.BLUE)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 10, 0, 0)
                }
            }
            addView(labelTextView)
            
            // دکمه بستن
            val closeButton = Button(this@CallerIdOverlayService).apply {
                text = "✕"
                textSize = 16f
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.RED)
                layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 10, 0, 0)
                }
                setOnClickListener { hideOverlay() }
            }
            addView(closeButton)
        }
    }
    
    /**
     * انیمیشن ورود
     */
    private fun showEnterAnimation() {
        overlayView?.let { view ->
            view.alpha = 0f
            view.scaleX = 0.8f
            view.scaleY = 0.8f
            
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(Constants.ANIMATION_DURATION)
                .start()
        }
    }
    
    /**
     * انیمیشن خروج
     */
    private fun showExitAnimation(onComplete: () -> Unit) {
        overlayView?.let { view ->
            view.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(Constants.OVERLAY_FADE_DURATION)
                .withEndAction(onComplete)
                .start()
        } ?: run {
            onComplete()
        }
    }
    
    /**
     * زمان‌بندی حذف خودکار
     */
    private fun scheduleAutoHide() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isOverlayShowing) {
                hideOverlay()
            }
        }, Constants.OVERLAY_DISPLAY_DURATION)
    }
    
    /**
     * مخفی کردن overlay
     * معادل دقیق Flutter hideCallerIdOverlay()
     */
    private fun hideOverlay() {
        if (!isOverlayShowing || overlayView == null) return
        
        try {
            showExitAnimation {
                windowManager?.removeView(overlayView)
                overlayView = null
                isOverlayShowing = false
                println("🔕 Overlay مخفی شد")
                
                // توقف سرویس
                stopSelf()
            }
        } catch (e: Exception) {
            println("❌ خطا در مخفی کردن overlay: $e")
            // حذف اجباری در صورت خطا
            try {
                windowManager?.removeView(overlayView)
            } catch (ignored: Exception) {}
            
            overlayView = null
            isOverlayShowing = false
            stopSelf()
        }
    }
    
    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }
}