package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

object ApkShareUtil {
    private const val TAG = "ApkShareUtil"
    private const val APK_FILE_NAME = "Natnael_Code_Editor_AI.apk"

    /**
     * Retrieves the formatted size of the installed app APK (e.g. "24.5 MB").
     */
    fun getApkSizeDisplay(context: Context): String {
        return try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (sourceApk.exists()) {
                val bytes = sourceApk.length()
                val mb = bytes.toDouble() / (1024 * 1024)
                String.format(Locale.US, "%.1f MB", mb)
            } else {
                "APK"
            }
        } catch (e: Exception) {
            "APK"
        }
    }

    /**
     * Copies the installed application APK to the cache directory and launches
     * the system share sheet with MIME type application/vnd.android.package-archive.
     */
    suspend fun shareAppApk(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val appInfo = context.applicationInfo
            val sourceApk = File(appInfo.sourceDir)

            if (!sourceApk.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Cannot find installed APK package.", Toast.LENGTH_SHORT).show()
                }
                return@withContext Result.failure(IllegalStateException("Source APK file not found"))
            }

            // Target directory inside cacheDir exposed by FileProvider
            val shareDir = File(context.cacheDir, "shared_apk").apply {
                if (!exists()) mkdirs()
            }
            val destinationApk = File(shareDir, APK_FILE_NAME)

            // Copy package archive to cache
            sourceApk.copyTo(destinationApk, overwrite = true)

            // Generate content URI with FileProvider
            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, destinationApk)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "Natnael Code Editor AI - APK")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Install Natnael Code Editor AI to write, debug, and build code with Gemini AI directly on your smartphone!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Natnael Code APK").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(chooserIntent)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share APK", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error sharing APK: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
            Result.failure(e)
        }
    }
}
