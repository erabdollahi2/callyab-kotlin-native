package ir.callyab.native.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.*
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import ir.callyab.native.models.Contact
import ir.callyab.native.utils.Constants
import ir.callyab.native.utils.Helpers
import java.io.*
import java.util.zip.ZipInputStream

/**
 * سرویس مدیریت فایل‌های CSV و Excel
 * معادل دقیق Flutter csv_service.dart + excel_service.dart
 */
class CsvService private constructor(private val context: Context) {
    
    private val contactService = UniversalContactService.getInstance(context)
    
    companion object {
        @Volatile
        private var INSTANCE: CsvService? = null
        
        fun getInstance(context: Context): CsvService {
            return INSTANCE ?: synchronized(this) {
                val instance = CsvService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * انتخاب فایل CSV از storage
     * معادل Flutter pickAndReadCsvFile()
     */
    suspend fun pickAndReadCsvFile(fileUri: Uri): Result<List<Contact>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return@withContext Result.failure(Exception("نمی‌توان فایل را باز کرد"))
                
            val contacts = readCsvFromInputStream(inputStream)
            inputStream.close()
            
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * خواندن CSV از assets
     * معادل Flutter loadContactsFromCSV()
     */
    suspend fun loadContactsFromAssets(): List<Contact> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("${Constants.ASSETS_DATA_PATH}${Constants.CSV_FILE_NAME}")
            val contacts = readCsvFromInputStream(inputStream)
            inputStream.close()
            contacts
        } catch (e: Exception) {
            println("❌ خطا در خواندن CSV از assets: $e")
            emptyList()
        }
    }
    
    /**
     * خواندن CSV از InputStream
     * معادل دقیق Flutter CSV parsing logic
     */
    private suspend fun readCsvFromInputStream(inputStream: InputStream): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        var rowCount = 0
        
        try {
            val reader = CSVReader(InputStreamReader(inputStream, "UTF-8"))
            val allRows = reader.readAll()
            
            // ردیف اول را header در نظر بگیریم
            val headers = allRows.firstOrNull() ?: return@withContext emptyList()
            
            println("📊 CSV Headers: ${headers.joinToString(", ")}")
            println("📊 Total rows: ${allRows.size - 1}")
            
            for (i in 1 until allRows.size) {
                if (rowCount >= Constants.CSV_MAX_ROWS) {
                    println("⚠️ محدودیت تعداد ردیف‌ها ($Constants.CSV_MAX_ROWS) رسید")
                    break
                }
                
                val row = allRows[i]
                if (row.size >= 2) { // حداقل کد ملی و شماره کارت
                    try {
                        val contact = parseRowToContact(row, headers)
                        if (contact != null) {
                            contacts.add(contact)
                            rowCount++
                            
                            // Progress report
                            if (rowCount % Constants.CSV_BATCH_SIZE == 0) {
                                println("📊 پردازش شد: $rowCount رکورد")
                            }
                        }
                    } catch (e: Exception) {
                        println("❌ خطا در پردازش ردیف $i: ${e.message}")
                    }
                }
            }
            
            reader.close()
            println("✅ CSV پردازش شد: ${contacts.size} مخاطب معتبر از ${allRows.size - 1} ردیف")
            
        } catch (e: Exception) {
            println("❌ خطا در خواندن CSV: $e")
        }
        
        contacts
    }
    
    /**
     * تبدیل ردیف CSV به مخاطب
     * معادل Flutter row parsing logic
     */
    private fun parseRowToContact(row: Array<String>, headers: Array<String>): Contact? {
        try {
            // نقشه‌برداری ستون‌ها
            val nationalCodeIndex = findColumnIndex(headers, listOf("کد ملی", "national_code", "kode_meli", "کدملی"))
            val cardNoIndex = findColumnIndex(headers, listOf("شماره کارت", "card_no", "shomare_kart", "کارت"))
            val fullNameIndex = findColumnIndex(headers, listOf("نام", "full_name", "name", "نام کامل"))
            val mobileIndex = findColumnIndex(headers, listOf("موبایل", "mobile", "phone", "شماره"))
            val birthDateIndex = findColumnIndex(headers, listOf("تاریخ تولد", "birth_date", "birthday", "تولد"))
            
            // بررسی وجود فیلدهای اجباری
            if (nationalCodeIndex == -1 || cardNoIndex == -1) {
                return null
            }
            
            val nationalCode = row.getOrNull(nationalCodeIndex)?.trim() ?: ""
            val cardNo = row.getOrNull(cardNoIndex)?.trim() ?: ""
            
            // اعتبارسنجی
            if (!Helpers.validateNationalCode(nationalCode) || cardNo.length != Constants.CARD_NUMBER_LENGTH) {
                return null
            }
            
            val fullName = row.getOrNull(fullNameIndex)?.trim()
            val mobile = row.getOrNull(mobileIndex)?.trim()?.let {
                if (Helpers.validateMobile(it)) Helpers.cleanPhoneNumber(it) else null
            }
            val birthDate = row.getOrNull(birthDateIndex)?.trim()
            
            return Contact(
                nationalCode = nationalCode,
                cardNo = cardNo,
                fullName = fullName,
                mobile = mobile,
                birthDate = birthDate
            )
            
        } catch (e: Exception) {
            println("❌ خطا در پارس کردن ردیف: $e")
            return null
        }
    }
    
    /**
     * پیدا کردن شاخص ستون
     */
    private fun findColumnIndex(headers: Array<String>, possibleNames: List<String>): Int {
        possibleNames.forEach { name ->
            val index = headers.indexOfFirst { it.contains(name, ignoreCase = true) }
            if (index >= 0) return index
        }
        return -1
    }
    
    /**
     * جستجو در مخاطبین
     * معادل Flutter searchContacts()
     */
    suspend fun searchContacts(contacts: List<Contact>, query: String): List<Contact> = withContext(Dispatchers.IO) {
        if (query.length < Constants.MIN_SEARCH_LENGTH) {
            return@withContext emptyList()
        }
        
        val searchTerm = query.lowercase().trim()
        
        contacts.filter { contact ->
            contact.nationalCode.contains(searchTerm) ||
            contact.cardNo.contains(searchTerm) ||
            contact.mobile?.contains(searchTerm) == true ||
            contact.fullName?.lowercase()?.contains(searchTerm) == true
        }.sortedBy { contact ->
            // اولویت‌بندی نتایج
            when {
                contact.nationalCode == query -> 1
                contact.mobile == query -> 2
                contact.cardNo == query -> 3
                contact.nationalCode.startsWith(searchTerm) -> 4
                contact.mobile?.startsWith(searchTerm) == true -> 5
                else -> 6
            }
        }.take(Constants.MAX_SEARCH_RESULTS)
    }
    
    /**
     * import کردن CSV به پایگاه داده
     * معادل Flutter importCsvToDatabase()
     */
    suspend fun importCsvToDatabase(fileUri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            
            // خواندن CSV
            val result = pickAndReadCsvFile(fileUri)
            if (result.isFailure) {
                return@withContext Result.failure(result.exceptionOrNull()!!)
            }
            
            val contacts = result.getOrNull() ?: emptyList()
            if (contacts.isEmpty()) {
                return@withContext Result.failure(Exception("هیچ مخاطب معتبری در فایل پیدا نشد"))
            }
            
            // اضافه کردن به پایگاه داده
            val addedCount = contactService.addContacts(contacts)
            
            val duration = System.currentTimeMillis() - startTime
            val importResult = ImportResult(
                totalRows = contacts.size,
                addedCount = addedCount,
                skippedCount = contacts.size - addedCount,
                duration = duration
            )
            
            Result.success(importResult)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * export کردن مخاطبین به CSV
     * معادل Flutter exportContactsToCsv()
     */
    suspend fun exportContactsToCsv(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val contacts = contactService.getAllContacts(limit = 10000)
            if (contacts.isEmpty()) {
                return@withContext Result.failure(Exception("هیچ مخاطبی برای خروجی وجود ندارد"))
            }
            
            val fileName = "callyab_backup_${System.currentTimeMillis()}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val writer = CSVWriter(FileWriter(file))
            
            // نوشتن header
            writer.writeNext(arrayOf("کد ملی", "شماره کارت", "نام کامل", "موبایل", "تاریخ تولد"))
            
            // نوشتن داده‌ها
            contacts.forEach { contact ->
                writer.writeNext(arrayOf(
                    contact.nationalCode,
                    contact.cardNo,
                    contact.fullName ?: "",
                    contact.mobile ?: "",
                    contact.birthDate ?: ""
                ))
            }
            
            writer.close()
            println("✅ خروجی CSV ایجاد شد: ${file.absolutePath}")
            
            Result.success(file)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * اشتراک‌گذاری فایل CSV
     */
    fun shareCsvFile(file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "پشتیبان کال یاب")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری فایل CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
    
    /**
     * آمار فایل CSV
     */
    suspend fun getCsvFileStats(fileUri: Uri): Result<CsvFileStats> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return@withContext Result.failure(Exception("نمی‌توان فایل را باز کرد"))
                
            val reader = CSVReader(InputStreamReader(inputStream, "UTF-8"))
            val allRows = reader.readAll()
            
            val totalRows = allRows.size - 1 // حذف header
            val fileSize = inputStream.available().toLong()
            
            reader.close()
            inputStream.close()
            
            val stats = CsvFileStats(
                totalRows = totalRows,
                fileSize = fileSize,
                fileName = fileUri.lastPathSegment ?: "نامشخص"
            )
            
            Result.success(stats)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * نتیجه import
 */
data class ImportResult(
    val totalRows: Int,
    val addedCount: Int,
    val skippedCount: Int,
    val duration: Long
)

/**
 * آمار فایل CSV
 */
data class CsvFileStats(
    val totalRows: Int,
    val fileSize: Long,
    val fileName: String
)