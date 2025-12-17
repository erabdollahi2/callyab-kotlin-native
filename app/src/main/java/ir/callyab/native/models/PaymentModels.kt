package ir.callyab.native.models

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * مدل بسته‌های خرید
 * معادل دقیق Flutter PaymentPlan class
 */
@Parcelize
data class PaymentPlan(
    val id: String,
    val uses: Int, // تعداد استفاده‌ها
    val priceInToman: Int, // قیمت به تومان
    val description: String
) : Parcelable {
    
    companion object {
        /**
         * بسته‌های از پیش تعریف شده
         * معادل دقیق Flutter paymentPlans
         */
        val paymentPlans = listOf(
            PaymentPlan(
                id = "plan_10",
                uses = 10,
                priceInToman = 50000,
                description = "10 بار استفاده"
            ),
            PaymentPlan(
                id = "plan_50", 
                uses = 50,
                priceInToman = 220000,
                description = "50 بار استفاده"
            ),
            PaymentPlan(
                id = "plan_100",
                uses = 100,
                priceInToman = 400000,
                description = "100 بار استفاده"
            )
        )
    }
}

/**
 * مدل تراکنش پرداخت
 * معادل دقیق Flutter PaymentTransaction class
 */
@Parcelize
@Entity(
    tableName = "payment_transactions",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["timestamp"])
    ]
)
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: String,

    /** به تومان */
    @ColumnInfo(name = "amount")
    val amount: Int,

    /** تعداد استفاده‌های اضافه شده */
    @ColumnInfo(name = "uses_added")
    val usesAdded: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: String = Date().time.toString(),

    @ColumnInfo(name = "success")
    val success: Boolean = false,

    /** شناسه تراکنش از درگاه */
    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null
) : Parcelable