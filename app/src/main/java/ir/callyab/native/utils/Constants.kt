package ir.callyab.native.utils

/**
 * ثوابت اپلیکیشن
 * معادل دقیق Flutter constants.dart
 */
object Constants {
    
    // نام اپلیکیشن
    const val APP_NAME = "کال یاب"
    const val APP_VERSION = "1.0.0"
    const val APP_PACKAGE = "ir.callyab.native"
    
    // پایگاه داده
    const val DATABASE_NAME = "contacts.db"
    const val DATABASE_VERSION = 1
    
    // SharedPreferences
    const val PREFS_NAME = "callyab_prefs"
    const val PREFS_USER_KEY = "current_user"
    const val PREFS_SETTINGS_KEY = "app_settings"
    
    // Cache
    const val CACHE_VALID_DURATION = 5 * 60 * 1000L // 5 minutes
    const val MAX_CACHE_SIZE = 1000
    
    // جستجو
    const val MIN_SEARCH_LENGTH = 3
    const val MAX_SEARCH_RESULTS = 50
    const val SEARCH_DELAY_MS = 500L
    
    // بسته‌های خرید - معادل Flutter payment.dart
    val PAYMENT_PLANS = listOf(
        Triple("plan_10", 10, 50000),    // 10 استفاده - 50,000 تومان
        Triple("plan_50", 50, 220000),   // 50 استفاده - 220,000 تومان  
        Triple("plan_100", 100, 400000)  // 100 استفاده - 400,000 تومان
    )
    
    // ادمین‌ها - معادل Flutter User.superAdminEmails
    val SUPER_ADMIN_EMAILS = listOf(
        "admin@callyab.ir",
        "support@callyab.ir", 
        "erabdollahi2@gmail.com"
    )
    
    // مسیرهای فایل
    const val ASSETS_DATA_PATH = "data/"
    const val CSV_FILE_NAME = "melli1.csv"
    const val BACKUP_FILE_NAME = "backup.csv"
    
    // HTTP و API
    const val API_BASE_URL = "https://api.callyab.ir/"
    const val API_TIMEOUT = 30_000L // 30 seconds
    const val RETRY_COUNT = 3
    
    // UI
    const val ANIMATION_DURATION = 300L
    const val TOAST_DURATION_SHORT = 2000
    const val TOAST_DURATION_LONG = 4000
    
    // Phone validation
    const val IRAN_COUNTRY_CODE = "+98"
    const val NATIONAL_CODE_LENGTH = 10
    const val MOBILE_NUMBER_LENGTH = 11
    const val CARD_NUMBER_LENGTH = 16
    
    // Permissions request codes
    const val PERMISSION_PHONE_STATE = 1001
    const val PERMISSION_READ_CONTACTS = 1002
    const val PERMISSION_CALL_LOG = 1003
    const val PERMISSION_OVERLAY = 1004
    const val PERMISSION_STORAGE = 1005
    
    // Activities request codes
    const val REQUEST_CODE_LOGIN = 2001
    const val REQUEST_CODE_FILE_PICKER = 2002
    const val REQUEST_CODE_CAMERA = 2003
    const val REQUEST_CODE_SETTINGS = 2004
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "callyab_channel"
    const val NOTIFICATION_CHANNEL_NAME = "کال یاب"
    const val NOTIFICATION_ID_CALLER_ID = 3001
    const val NOTIFICATION_ID_SERVICE = 3002
    
    // Background service
    const val SERVICE_START_DELAY = 2000L
    const val SERVICE_CHECK_INTERVAL = 10000L // 10 seconds
    
    // Overlay
    const val OVERLAY_DISPLAY_DURATION = 5000L // 5 seconds
    const val OVERLAY_FADE_DURATION = 1000L // 1 second
    
    // CSV Import
    const val CSV_MAX_ROWS = 100_000
    const val CSV_BATCH_SIZE = 1000
    const val CSV_IMPORT_TIMEOUT = 300_000L // 5 minutes
    
    // Error messages
    const val ERROR_NO_INTERNET = "اتصال اینترنت برقرار نیست"
    const val ERROR_DATABASE = "خطا در پایگاه داده"
    const val ERROR_PERMISSION_DENIED = "مجوز لازم داده نشده است"
    const val ERROR_FILE_NOT_FOUND = "فایل پیدا نشد"
    const val ERROR_INVALID_INPUT = "ورودی نامعتبر است"
    
    // Success messages
    const val SUCCESS_LOGIN = "ورود موفقیت‌آمیز"
    const val SUCCESS_LOGOUT = "خروج انجام شد"
    const val SUCCESS_SEARCH = "جستجو کامل شد"
    const val SUCCESS_IMPORT = "وارد کردن فایل موفق بود"
    const val SUCCESS_BACKUP = "پشتیبان‌گیری انجام شد"
    
    // Regular expressions
    val EMAIL_REGEX = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    val MOBILE_REGEX = Regex("^09\\d{9}$")
    val NATIONAL_CODE_REGEX = Regex("^\\d{10}$")
    val CARD_NUMBER_REGEX = Regex("^\\d{16}$")
    
    // Date formats
    const val DATE_FORMAT_PERSIAN = "yyyy/MM/dd"
    const val DATE_FORMAT_ENGLISH = "yyyy-MM-dd"
    const val DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    
    // File types
    val SUPPORTED_CSV_EXTENSIONS = arrayOf("csv", "txt")
    val SUPPORTED_EXCEL_EXTENSIONS = arrayOf("xls", "xlsx")
    val SUPPORTED_IMAGE_EXTENSIONS = arrayOf("jpg", "jpeg", "png")
}