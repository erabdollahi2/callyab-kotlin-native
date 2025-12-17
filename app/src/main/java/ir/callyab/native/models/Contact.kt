package ir.callyab.native.models

import androidx.room.*
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.random.Random

/**
 * مدل داده‌ای برای ذخیره‌سازی اطلاعات مخاطبین در SQLite
 * معادل دقیق Flutter Contact class
 */
@Parcelize
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["national_code"], unique = true),
        Index(value = ["mobile"]),
        Index(value = ["card_no"])
    ]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** کد ملی - 10 رقم */
    @ColumnInfo(name = "national_code")
    val nationalCode: String,

    /** شماره کارت - 16 رقم */
    @ColumnInfo(name = "card_no")
    val cardNo: String,

    /** نام کامل - متن فارسی */
    @ColumnInfo(name = "full_name")
    val fullName: String? = null,

    /** تاریخ تولد - فرمت شمسی (YYYY-MM-DD) */
    @ColumnInfo(name = "birth_date")
    val birthDate: String? = null,

    /** شماره موبایل - 10 رقم */
    @ColumnInfo(name = "mobile")
    val mobile: String? = null,

    /** تاریخ ایجاد رکورد */
    @ColumnInfo(name = "created_at")
    val createdAt: String = Date().time.toString()
) : Parcelable {

    companion object {
        /**
         * Constructor برای SQLite Map - معادل Flutter Contact.fromMap
         */
        fun fromMap(map: Map<String, Any?>): Contact {
            return Contact(
                nationalCode = map["national_code"]?.toString() ?: "",
                cardNo = map["card_no"]?.toString() ?: "",
                fullName = map["full_name"]?.toString(),
                birthDate = map["birth_date"]?.toString(),
                mobile = map["mobile"]?.toString(),
                createdAt = Date().time.toString()
            )
        }
    }

    /** 
     * بررسی کمپلیت بودن اطلاعات مخاطب 
     * معادل دقیق Flutter get isComplete
     */
    val isComplete: Boolean
        get() = nationalCode.isNotEmpty() &&
                cardNo.isNotEmpty() &&
                !fullName.isNullOrEmpty() &&
                !birthDate.isNullOrEmpty() &&
                !mobile.isNullOrEmpty()

    /**
     * تعداد فیلدهای پر شده
     * معادل دقیق Flutter get filledFieldsCount
     */
    val filledFieldsCount: Int
        get() {
            var count = 2 // nationalCode و cardNo همیشه پر هستند
            if (!fullName.isNullOrEmpty()) count++
            if (!birthDate.isNullOrEmpty()) count++
            if (!mobile.isNullOrEmpty()) count++
            return count
        }

    /**
     * تبدیل به Map برای ذخیره در SQLite
     * معادل دقیق Flutter toMap()
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "national_code" to nationalCode,
            "card_no" to cardNo,
            "full_name" to fullName,
            "birth_date" to birthDate,
            "mobile" to mobile,
            "created_at" to createdAt
        )
    }

    /**
     * ایجاد کپی با تغییرات
     * معادل دقیق Flutter copyWith()
     */
    fun copyWith(
        nationalCode: String? = null,
        cardNo: String? = null,
        fullName: String? = null,
        birthDate: String? = null,
        mobile: String? = null,
        createdAt: String? = null
    ): Contact {
        return Contact(
            id = this.id,
            nationalCode = nationalCode ?: this.nationalCode,
            cardNo = cardNo ?: this.cardNo,
            fullName = fullName ?: this.fullName,
            birthDate = birthDate ?: this.birthDate,
            mobile = mobile ?: this.mobile,
            createdAt = createdAt ?: this.createdAt
        )
    }
}