package ir.callyab.native.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import ir.callyab.native.R
import ir.callyab.native.activities.DashboardActivity
import ir.callyab.native.receivers.PhoneStateReceiver
import ir.callyab.native.utils.Constants

/**
 * سرویس پس‌زمینه برای caller ID
 * معادل دقیق Flutter caller_id_background_service.dart
 */
class CallerIdBackgroundService : Service() {
    
    private lateinit var phoneStateReceiver: PhoneStateReceiver
    private var isReceiverRegistered = false
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "caller_id_service_channel"
        
        fun start(context: Context) {
            val intent = Intent(context, CallerIdBackgroundService::class.java)
            context.startForegroundService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, CallerIdBackgroundService::class.java)
            context.stopService(intent)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerPhoneStateReceiver()
        println("✅ سرویس caller ID ایجاد شد")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        println("🔄 سرویس caller ID شروع شد")
        return START_STICKY
    }
    
    override fun onDestroy() {
        unregisterPhoneStateReceiver()
        println("🔴 سرویس caller ID متوقف شد")
        super.onDestroy()
    }
    
    /**
     * ایجاد کانال اعلان‌ها
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "سرویس شناسایی تماس",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "سرویس پس‌زمینه برای شناسایی تماس‌های ورودی"
            setSound(null, null)
            enableVibration(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * ایجاد اعلان
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("کال یاب فعال است")
            .setContentText("شناسایی تماس‌های ورودی در حال اجرا...")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(null)
            .setVibrate(null)
            .build()
    }
    
    /**
     * ثبت دریافت کننده وضعیت تماس
     * معادل دقیق Flutter registerPhoneStateListener()
     */
    private fun registerPhoneStateReceiver() {
        try {
            if (!isReceiverRegistered) {
                phoneStateReceiver = PhoneStateReceiver()
                
                val intentFilter = IntentFilter().apply {
                    addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                    priority = IntentFilter.SYSTEM_HIGH_PRIORITY
                }
                
                registerReceiver(phoneStateReceiver, intentFilter)
                isReceiverRegistered = true
                
                println("📱 دریافت کننده وضعیت تماس ثبت شد")
            }
        } catch (e: Exception) {
            println("❌ خطا در ثبت دریافت کننده: $e")
        }
    }
    
    /**
     * لغو ثبت دریافت کننده وضعیت تماس
     * معادل دقیق Flutter unregisterPhoneStateListener()
     */
    private fun unregisterPhoneStateReceiver() {
        try {
            if (isReceiverRegistered) {
                unregisterReceiver(phoneStateReceiver)
                isReceiverRegistered = false
                println("📱 دریافت کننده وضعیت تماس لغو شد")
            }
        } catch (e: Exception) {
            println("❌ خطا در لغو دریافت کننده: $e")
        }
    }
}