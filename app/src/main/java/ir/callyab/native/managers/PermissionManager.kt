package ir.callyab.native.managers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import ir.callyab.native.utils.Constants

/**
 * مدیر مجوزها
 * معادل دقیق Flutter permission_manager.dart
 */
class PermissionManager(private val context: Context) {
    
    /**
     * کالبک های مجوزها
     */
    interface PermissionCallback {
        fun onPermissionsGranted()
        fun onPermissionsDenied(deniedPermissions: List<String>)
        fun onPermissionError(error: String)
    }
    
    companion object {
        /**
         * مجوزهای مورد نیاز برای عملکرد کامل اپلیکیشن
         * معادل دقیق Flutter _requiredPermissions
         */
        private val REQUIRED_PERMISSIONS = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        
        /**
         * مجوزهای اختیاری
         */
        private val OPTIONAL_PERMISSIONS = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
    
    /**
     * بررسی وضعیت همه مجوزهای ضروری
     * معادل دقیق Flutter checkAllPermissions()
     */
    fun checkAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * بررسی مجوز overlay
     * معادل دقیق Flutter checkOverlayPermission()
     */
    fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    /**
     * درخواست همه مجوزهای ضروری
     * معادل دقیق Flutter requestAllPermissions()
     */
    fun requestAllPermissions(callback: PermissionCallback) {
        // بررسی اولیه
        if (checkAllPermissions()) {
            callback.onPermissionsGranted()
            return
        }
        
        Dexter.withContext(context)
            .withPermissions(REQUIRED_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    when {
                        report.areAllPermissionsGranted() -> {
                            println("✅ همه مجوزها اعطا شد")
                            callback.onPermissionsGranted()
                        }
                        
                        report.isAnyPermissionPermanentlyDenied -> {
                            val deniedPermissions = report.deniedPermissionResponses.map { it.permissionName }
                            println("❌ مجوزهای رد شده: $deniedPermissions")
                            callback.onPermissionsDenied(deniedPermissions)
                            
                            // هدایت به تنظیمات
                            showPermissionSettingsDialog()
                        }
                        
                        else -> {
                            val deniedPermissions = report.deniedPermissionResponses.map { it.permissionName }
                            println("⚠️ مجوزهای رد شده موقت: $deniedPermissions")
                            callback.onPermissionsDenied(deniedPermissions)
                        }
                    }
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    // نمایش توضیحات برای کاربر
                    showPermissionRationale(permissions, token)
                }
            })
            .check()
    }
    
    /**
     * درخواست مجوز overlay
     * معادل دقیق Flutter requestOverlayPermission()
     */
    fun requestOverlayPermission(callback: PermissionCallback) {
        if (checkOverlayPermission()) {
            callback.onPermissionsGranted()
            return
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            println("❌ خطا در درخواست مجوز overlay: $e")
            callback.onPermissionError("خطا در درخواست مجوز overlay: ${e.message}")
        }
    }
    
    /**
     * بررسی مجوز خاص
     * معادل دقیق Flutter checkSinglePermission()
     */
    fun checkSinglePermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * درخواست مجوز خاص
     * معادل دقیق Flutter requestSinglePermission()
     */
    fun requestSinglePermission(permission: String, callback: PermissionCallback) {
        if (checkSinglePermission(permission)) {
            callback.onPermissionsGranted()
            return
        }
        
        Dexter.withContext(context)
            .withPermission(permission)
            .withListener(object : com.karumi.dexter.listener.single.PermissionListener {
                override fun onPermissionGranted(response: com.karumi.dexter.listener.single.PermissionGrantedResponse) {
                    println("✅ مجوز $permission اعطا شد")
                    callback.onPermissionsGranted()
                }
                
                override fun onPermissionDenied(response: com.karumi.dexter.listener.single.PermissionDeniedResponse) {
                    println("❌ مجوز $permission رد شد")
                    callback.onPermissionsDenied(listOf(permission))
                    
                    if (response.isPermanentlyDenied) {
                        showPermissionSettingsDialog()
                    }
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    showPermissionRationale(listOf(permission), token)
                }
            })
            .check()
    }
    
    /**
     * نمایش توضیحات برای مجوزها
     */
    private fun showPermissionRationale(permissions: List<PermissionRequest>, token: PermissionToken) {
        val permissionNames = permissions.map { getPermissionDisplayName(it.name) }
        
        if (context is Activity) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("نیاز به مجوز")
                .setMessage("برای عملکرد صحیح اپلیکیشن، مجوزهای زیر مورد نیاز است:\n\n${permissionNames.joinToString("\n")}")
                .setPositiveButton("اعطا مجوز") { _, _ -> token.continuePermissionRequest() }
                .setNegativeButton("انصراف") { _, _ -> token.cancelPermissionRequest() }
                .setCancelable(false)
                .show()
        } else {
            token.continuePermissionRequest()
        }
    }
    
    /**
     * نمایش دایالوگ هدایت به تنظیمات
     */
    private fun showPermissionSettingsDialog() {
        if (context is Activity) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("مجوزهای مورد نیاز")
                .setMessage("برای عملکرد صحیح اپلیکیشن، لطفاً مجوزهای مورد نیاز را در تنظیمات فعال کنید.")
                .setPositiveButton("تنظیمات") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
                .setNegativeButton("انصراف", null)
                .show()
        }
    }
    
    /**
     * دریافت نام نمایشی مجوز
     */
    private fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_PHONE_STATE -> "• دسترسی به وضعیت تماس"
            Manifest.permission.READ_CALL_LOG -> "• خواندن سابقه تماس"
            Manifest.permission.READ_CONTACTS -> "• خواندن مخاطبین"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "• نوشتن در حافظه"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "• خواندن از حافظه"
            Manifest.permission.CAMERA -> "• دوربین"
            Manifest.permission.RECORD_AUDIO -> "• ضبط صدا"
            else -> "• $permission"
        }
    }
    
    /**
     * دریافت لیست مجوزهای رد شده
     * معادل دقیق Flutter getDeniedPermissions()
     */
    fun getDeniedPermissions(): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * دریافت لیست مجوزهای اعطا شده
     * معادل دقیق Flutter getGrantedPermissions()
     */
    fun getGrantedPermissions(): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * بررسی وضعیت مجوزهای ضروری برای caller ID
     * معادل دقیق Flutter checkCallerIdPermissions()
     */
    fun checkCallerIdPermissions(): Boolean {
        val callerIdPermissions = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
        
        return callerIdPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } && checkOverlayPermission()
    }
    
    /**
     * درخواست مجوزهای ضروری برای caller ID
     * معادل دقیق Flutter requestCallerIdPermissions()
     */
    fun requestCallerIdPermissions(callback: PermissionCallback) {
        val callerIdPermissions = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
        
        Dexter.withContext(context)
            .withPermissions(callerIdPermissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        // حالا درخواست مجوز overlay
                        requestOverlayPermission(callback)
                    } else {
                        val deniedPermissions = report.deniedPermissionResponses.map { it.permissionName }
                        callback.onPermissionsDenied(deniedPermissions)
                    }
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showPermissionRationale(permissions, token)
                }
            })
            .check()
    }
}