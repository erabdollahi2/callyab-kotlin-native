package ir.callyab.native.utils

import android.content.Context
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * کلاس کمکی برای عملیات مختلف
 * معادل دقیق Flutter helpers.dart
 */
object Helpers {
    
    /**
     * نمایش Toast
     */
    fun showToast(context: Context, message: String, isLong: Boolean = false) {
        Toast.makeText(
            context, 
            message, 
            if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }
    
    /**
     * بررسی اتصال اینترنت
     * معادل Flutter connectivity check
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    /**
     * اعتبارسنجی کد ملی
     * معادل دقیق Flutter validateNationalCode()
     */
    fun validateNationalCode(nationalCode: String): Boolean {
        if (TextUtils.isEmpty(nationalCode) || nationalCode.length != 10) {
            return false
        }
        
        if (!nationalCode.all { it.isDigit() }) {
            return false
        }
        
        // بررسی کدهای تکراری
        val invalidCodes = listOf(
            "0000000000", "1111111111", "2222222222", "3333333333", "4444444444",
            "5555555555", "6666666666", "7777777777", "8888888888", "9999999999"
        )
        
        if (nationalCode in invalidCodes) {
            return false
        }
        
        // محاسبه چک دیجیت
        var sum = 0
        for (i in 0..8) {
            sum += (nationalCode[i].toString().toInt()) * (10 - i)
        }
        
        val remainder = sum % 11
        val checkDigit = nationalCode[9].toString().toInt()
        
        return if (remainder < 2) {
            checkDigit == remainder
        } else {
            checkDigit == (11 - remainder)
        }
    }
    
    /**
     * اعتبارسنجی شماره موبایل
     * معادل دقیق Flutter validateMobile()
     */
    fun validateMobile(mobile: String): Boolean {
        if (TextUtils.isEmpty(mobile)) return false
        
        val cleanMobile = cleanPhoneNumber(mobile)
        return Constants.MOBILE_REGEX.matches(cleanMobile)
    }
    
    /**
     * اعتبارسنجی ایمیل
     */
    fun validateEmail(email: String): Boolean {
        return Constants.EMAIL_REGEX.matches(email.trim().lowercase())
    }
    
    /**
     * اعتبارسنجی شماره کارت
     */
    fun validateCardNumber(cardNumber: String): Boolean {
        val cleanCard = cardNumber.replace("\\s".toRegex(), "")
        return Constants.CARD_NUMBER_REGEX.matches(cleanCard)
    }
    
    /**
     * تمیز کردن شماره تلفن
     * معادل دقیق Flutter normalizePhoneNumber()
     */
    fun cleanPhoneNumber(phoneNumber: String): String {
        var clean = phoneNumber.replace(Regex("[^0-9]"), "")
        
        // حذف کد کشور ایران
        if (clean.startsWith("98")) {
            clean = "0" + clean.substring(2)
        }
        
        // اضافه کردن صفر در ابتدا
        if (!clean.startsWith("0") && clean.length == 10) {
            clean = "0$clean"
        }
        
        return clean
    }
    
    /**
     * فرمت کردن قیمت با جداکننده هزارگان
     * معادل Flutter formatPrice()
     */
    fun formatPrice(price: Int): String {
        val formatter = DecimalFormat("#,###")
        return "${formatter.format(price)} تومان"
    }
    
    /**
     * فرمت کردن تاریخ
     */
    fun formatDate(timestamp: Long, pattern: String = Constants.DATE_FORMAT_PERSIAN): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(pattern, Locale("fa", "IR"))
        return formatter.format(date)
    }
    
    /**
     * تبدیل تاریخ شمسی به میلادی
     */
    fun persianToGregorian(persianDate: String): String {
        // پیاده‌سازی ساده - در پروژه واقعی از کتابخانه تقویم استفاده کنید
        return persianDate.replace("/", "-")
    }
    
    /**
     * محاسبه فاصله زمانی
     */
    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "همین حالا"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} دقیقه پیش"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} ساعت پیش"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} روز پیش"
            else -> formatDate(timestamp)
        }
    }
    
    /**
     * ایجاد ID یکتا
     * معادل Flutter contact ID generation
     */
    fun generateUniqueId(): String {
        return "${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * رمزگذاری ساده پسورد
     */
    fun hashPassword(password: String): String {
        return password.hashCode().toString()
    }
    
    /**
     * بررسی قوت رمز عبور
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        return when {
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 8 -> PasswordStrength.MEDIUM
            password.any { it.isUpperCase() } && 
            password.any { it.isLowerCase() } && 
            password.any { it.isDigit() } -> PasswordStrength.STRONG
            else -> PasswordStrength.MEDIUM
        }
    }
    
    /**
     * انداز فایل به مگابایت
     */
    fun formatFileSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format("%.2f MB", mb)
    }
    
    /**
     * بررسی نام فایل معتبر
     */
    fun isValidFileName(fileName: String): Boolean {
        val invalidChars = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
        return fileName.isNotEmpty() && !fileName.any { it in invalidChars }
    }
    
    /**
     * استخراج پسوند فایل
     */
    fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot >= 0) {
            fileName.substring(lastDot + 1).lowercase()
        } else {
            ""
        }
    }
    
    /**
     * تولید رنگ بر اساس متن
     */
    fun getColorFromString(text: String): Int {
        val hash = abs(text.hashCode())
        val colors = intArrayOf(
            0xFF2196F3.toInt(), // Blue
            0xFF4CAF50.toInt(), // Green
            0xFFFF9800.toInt(), // Orange
            0xFF9C27B0.toInt(), // Purple
            0xFFF44336.toInt(), // Red
            0xFF00BCD4.toInt(), // Cyan
            0xFFFFEB3B.toInt(), // Yellow
            0xFF795548.toInt()  // Brown
        )
        return colors[hash % colors.size]
    }
    
    /**
     * بررسی RTL برای متن فارسی
     */
    fun isRtlText(text: String): Boolean {
        return text.any { char ->
            char in '\u0600'..'\u06FF' || char in '\u0750'..'\u077F'
        }
    }
    
    /**
     * انتقال متن به کلیپ‌بورد
     */
    fun copyToClipboard(context: Context, text: String, label: String = "کپی شده") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        showToast(context, "$label کپی شد")
    }
}

/**
 * قدرت رمز عبور
 */
enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}