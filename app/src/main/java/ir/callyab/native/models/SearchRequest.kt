package ir.callyab.native.models

import androidx.room.*
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * مدل درخواست جستجو
 * معادل دقیق Flutter SearchRequest class
 */
@Parcelize
@Entity(
    tableName = "search_requests",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["requested_at"])
    ]
)
data class SearchRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** email کاربر */
    @ColumnInfo(name = "user_id")
    val userId: String,

    /** کوئری جستجو */
    @ColumnInfo(name = "search_query")
    val searchQuery: String,

    /** نام مخاطب پیدا شده */
    @ColumnInfo(name = "result_contact_name")
    val resultContactName: String? = null,

    /** تاریخ درخواست */
    @ColumnInfo(name = "requested_at")
    val requestedAt: String = Date().time.toString(),

    /** آیا نتیجه‌ای پیدا شد */
    @ColumnInfo(name = "success")
    val success: Boolean = false
) : Parcelable