package ir.callyab.native.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.callyab.native.services.CallerIdBackgroundService

/**
 * دریافت کننده بوت سیستم
 * معادل دقیق Flutter boot_receiver.dart
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action || 
            Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
            
            println("🔄 سیستم راه‌اندازی شد - شروع سرویس caller ID")
            
            // راه‌اندازی سرویس caller ID بعد از بوت
            val serviceIntent = Intent(context, CallerIdBackgroundService::class.java)
            context.startForegroundService(serviceIntent)
            
            println("✅ سرویس caller ID شروع شد")
        }
    }
}