package ir.callyab.native.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import ir.callyab.native.models.Contact

/**
 * سرویس تشخیص هویت تماس گیرنده (Caller ID)
 * معادل دقیق Flutter CallerIdService
 */
class CallerIdService private constructor(private val context: Context) {
    
    private val universalContactService = UniversalContactService.getInstance(context)
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private var phoneStateListener: PhoneStateListener? = null
    private var isServiceActive = false
    private var contactsCache: List<Contact> = emptyList()
    private var lastCacheUpdate = 0L
    
    companion object {
        @Volatile
        private var INSTANCE: CallerIdService? = null
        
        fun getInstance(context: Context): CallerIdService {
            return INSTANCE ?: synchronized(this) {
                val instance = CallerIdService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * شروع سرویس Caller ID
     * معادل دقیق Flutter startCallerIdService()
     */
    suspend fun startCallerIdService(): Boolean = withContext(Dispatchers.Main) {
        try {
            // بررسی و درخواست permissions
            if (!checkPermissions()) {
                return@withContext false
            }
            
            // بارگیری cache مخاطبین
            loadContactsCache()
            
            // شروع listening به phone state
            startPhoneStateListening()
            
            isServiceActive = true
            println("🟢 Caller ID Service شروع شد")
            return@withContext true
        } catch (e: Exception) {
            println("❌ خطا در شروع Caller ID Service: $e")
            return@withContext false
        }
    }
    
    /**
     * توقف سرویس
     * معادل دقیق Flutter stopCallerIdService()
     */
    fun stopCallerIdService() {
        phoneStateListener?.let {
            telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
        }
        phoneStateListener = null
        isServiceActive = false
        println("🔴 Caller ID Service متوقف شد")
    }
    
    /**
     * بررسی وضعیت سرویس
     * معادل دقیق Flutter isActive
     */
    val isActive: Boolean
        get() = isServiceActive
    
    /**
     * بررسی permissions
     * معادل دقیق Flutter _checkPermissions()
     */
    private fun checkPermissions(): Boolean {
        // Phone State Permission
        val phonePermission = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.READ_PHONE_STATE
        )
        if (phonePermission != PackageManager.PERMISSION_GRANTED) {
            println("❌ Phone permission not granted")
            return false
        }
        
        // Call Log Permission
        val callLogPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        )
        if (callLogPermission != PackageManager.PERMISSION_GRANTED) {
            println("❌ Call log permission not granted")
            return false
        }
        
        return true
    }
    
    /**
     * شروع گوش دادن به وضعیت تلفن
     */
    private fun startPhoneStateListening() {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                super.onCallStateChanged(state, phoneNumber)
                
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        // تماس ورودی
                        handleIncomingCall(phoneNumber)
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        // تماس شروع شد
                        handleCallStarted()
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        // تماس پایان یافت
                        handleCallEnded()
                    }
                }
            }
        }
        
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }
    
    /**
     * مدیریت تماس ورودی
     * معادل دقیق Flutter _handleIncomingCall()
     */
    private fun handleIncomingCall(phoneNumber: String?) {
        if (phoneNumber.isNullOrEmpty()) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // نرمال‌سازی شماره تلفن
                val normalizedNumber = normalizePhoneNumber(phoneNumber)
                
                // جستجو در مخاطبین
                val contact = findContactByPhone(normalizedNumber)
                
                if (contact != null) {
                    println("📞 تماس ورودی از: ${contact.fullName} (${contact.mobile})")
                    showCallerIdOverlay(contact)
                } else {
                    println("📞 تماس ورودی از شماره ناشناس: $normalizedNumber")
                    showUnknownCallerOverlay(normalizedNumber)
                }
            } catch (e: Exception) {
                println("❌ خطا در پردازش تماس ورودی: $e")
            }
        }
    }
    
    /**
     * مدیریت شروع تماس
     * معادل دقیق Flutter _handleCallStarted()
     */
    private fun handleCallStarted() {
        println("📞 تماس شروع شد")
    }
    
    /**
     * مدیریت پایان تماس
     * معادل دقیق Flutter _handleCallEnded()
     */
    private fun handleCallEnded() {
        println("📞 تماس پایان یافت")
        // بستن overlay در صورت نمایش
        hideCallerIdOverlay()
    }
    
    /**
     * بارگیری cache مخاطبین
     * معادل دقیق Flutter _loadContactsCache()
     */
    private suspend fun loadContactsCache() {
        try {
            val now = System.currentTimeMillis()
            if (contactsCache.isNotEmpty() && (now - lastCacheUpdate) < 300000) { // 5 minutes
                return // Cache هنوز معتبر است
            }
            
            contactsCache = universalContactService.getAllContacts(limit = 1000)
            lastCacheUpdate = now
            println("✅ Cache مخاطبین بارگیری شد: ${contactsCache.size} مخاطب")
        } catch (e: Exception) {
            println("❌ خطا در بارگیری cache: $e")
        }
    }
    
    /**
     * جستجو مخاطب بر اساس شماره تلفن
     * معادل دقیق Flutter _findContactByPhone()
     */
    private suspend fun findContactByPhone(phoneNumber: String): Contact? {
        try {
            // اول از cache جستجو کن
            var contact = contactsCache.find { it.mobile == phoneNumber }
            if (contact != null) return contact
            
            // اگر در cache نبود، از پایگاه داده جستجو کن
            contact = universalContactService.searchContactByMobile(phoneNumber)
            if (contact != null) return contact
            
            // جستجوی عمومی
            val contacts = universalContactService.searchContacts(phoneNumber, 1)
            return contacts.firstOrNull()
        } catch (e: Exception) {
            println("❌ خطا در جستجوی مخاطب: $e")
            return null
        }
    }
    
    /**
     * نرمال‌سازی شماره تلفن
     * معادل دقیق Flutter _normalizePhoneNumber()
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        var normalized = phoneNumber.replace(Regex("[^0-9]"), "")
        
        // حذف کد کشور ایران
        if (normalized.startsWith("98")) {
            normalized = "0" + normalized.substring(2)
        }
        
        // اطمینان از شروع با 0
        if (!normalized.startsWith("0")) {
            normalized = "0$normalized"
        }
        
        return normalized
    }
    
    /**
     * نمایش overlay شناسایی تماس گیرنده
     * معادل دقیق Flutter _showCallerIdOverlay()
     */
    private suspend fun showCallerIdOverlay(contact: Contact) = withContext(Dispatchers.Main) {
        try {
            println("🔔 نمایش اطلاعات تماس گیرنده: ${contact.fullName}")
            // TODO: پیاده‌سازی overlay UI
            // می‌توان از Dialog، Toast، یا Floating Window استفاده کرد
        } catch (e: Exception) {
            println("❌ خطا در نمایش overlay: $e")
        }
    }
    
    /**
     * نمایش overlay برای تماس ناشناس
     * معادل دقیق Flutter _showUnknownCallerOverlay()
     */
    private suspend fun showUnknownCallerOverlay(phoneNumber: String) = withContext(Dispatchers.Main) {
        try {
            println("❓ نمایش تماس ناشناس: $phoneNumber")
            // TODO: پیاده‌سازی overlay UI برای تماس ناشناس
        } catch (e: Exception) {
            println("❌ خطا در نمایش overlay ناشناس: $e")
        }
    }
    
    /**
     * مخفی کردن overlay
     * معادل دقیق Flutter hideCallerIdOverlay()
     */
    private fun hideCallerIdOverlay() {
        try {
            println("🔕 مخفی کردن overlay تماس")
            // TODO: بستن overlay
        } catch (e: Exception) {
            println("❌ خطا در مخفی کردن overlay: $e")
        }
    }
    
    /**
     * دریافت تنظیمات سرویس
     * معادل دقیق Flutter getServiceSettings()
     */
    suspend fun getServiceSettings(): Map<String, Any> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "enabled" to isServiceActive,
            "cacheSize" to contactsCache.size,
            "lastUpdate" to lastCacheUpdate,
            "hasPermissions" to checkPermissions()
        )
    }
}