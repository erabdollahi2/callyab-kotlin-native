package ir.callyab.native.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import ir.callyab.native.overlay.CallerIdOverlayService
import ir.callyab.native.services.CallerIdService
import ir.callyab.native.utils.Helpers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * دریافت کننده وضعیت تماس
 * معادل دقیق Flutter phone_state_receiver.dart
 */
class PhoneStateReceiver : BroadcastReceiver() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // تماس ورودی - معادل PhoneState.ringing در Flutter
                handleIncomingCall(context, incomingNumber)
                println("📞 تماس ورودی شناسایی شد: $incomingNumber")
            }
            
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // تماس برقرار - معادل PhoneState.offHook در Flutter
                handleCallPickedUp(context)
                println("📞 تماس برقرار شد")
            }
            
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // تماس قطع - معادل PhoneState.idle در Flutter
                handleCallEnded(context)
                println("📞 تماس پایان یافت")
            }
        }
    }
    
    /**
     * مدیریت تماس ورودی
     * معادل دقیق Flutter handleIncomingCall()
     */
    private fun handleIncomingCall(context: Context, phoneNumber: String?) {
        if (phoneNumber.isNullOrBlank()) {
            println("❌ شماره تماس خالی است")
            return
        }
        
        // نرمال‌سازی شماره تماس
        val normalizedNumber = Helpers.normalizePhoneNumber(phoneNumber)
        
        serviceScope.launch {
            try {
                // جستجوی مخاطب در پایگاه داده
                val callerIdService = CallerIdService.getInstance(context)
                val contact = callerIdService.findContactByPhone(normalizedNumber)
                
                if (contact != null) {
                    // نمایش overlay برای مخاطب شناخته شده
                    CallerIdOverlayService.showCallerInfo(context, contact)
                    println("✅ مخاطب یافت شد: ${contact.fullName}")
                } else {
                    // نمایش overlay برای مخاطب ناشناس
                    CallerIdOverlayService.showUnknownCaller(context, normalizedNumber)
                    println("❓ مخاطب یافت نشد برای: $normalizedNumber")
                }
                
                // ذخیره سابقه تماس
                callerIdService.logCall(normalizedNumber, contact?.id)
                
            } catch (e: Exception) {
                println("❌ خطا در پردازش تماس ورودی: $e")
            }
        }
    }
    
    /**
     * مدیریت برداشتن گوشی
     * معادل دقیق Flutter handleCallPickedUp()
     */
    private fun handleCallPickedUp(context: Context) {
        // مخفی کردن overlay در صورت برداشتن گوشی
        CallerIdOverlayService.hideOverlay(context)
        println("🔕 Overlay به دلیل برداشتن گوشی مخفی شد")
    }
    
    /**
     * مدیریت پایان تماس
     * معادل دقیق Flutter handleCallEnded()
     */
    private fun handleCallEnded(context: Context) {
        // مخفی کردن overlay در صورت پایان تماس
        CallerIdOverlayService.hideOverlay(context)
        println("🔕 Overlay به دلیل پایان تماس مخفی شد")
    }
}