package com.example.medicaldocuments.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

class BackupManager(private val context: Context) {

    fun shareBackupFile(backupFile: File) {
        val authority = "${context.packageName}.fileprovider"

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                authority,
                backupFile
            )
        } else {
            Uri.fromFile(backupFile)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Поделиться бэкапом"))
    }
}