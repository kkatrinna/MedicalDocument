package com.example.medicaldocuments.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File

object FileUtils {

    fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var fileName: String? = null

        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = uri.path?.substringAfterLast('/')
        }

        Log.d("FileUtils", "Original file name: $fileName")

        if (fileName != null && !fileName!!.contains('.')) {
            val mimeType = contentResolver.getType(uri)
            Log.d("FileUtils", "MIME type: $mimeType")

            val extension = when {
                mimeType == null -> ""
                mimeType.contains("word") ||
                        mimeType.contains("msword") ||
                        mimeType.contains("wordprocessingml") ||
                        mimeType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        mimeType.contains("application/msword") -> ".docx"

                mimeType.contains("pdf") -> ".pdf"

                mimeType.contains("image") -> {
                    when {
                        mimeType.contains("png") -> ".png"
                        mimeType.contains("jpeg") -> ".jpg"
                        mimeType.contains("gif") -> ".gif"
                        mimeType.contains("bmp") -> ".bmp"
                        mimeType.contains("webp") -> ".webp"
                        else -> ".jpg"
                    }
                }
                mimeType.contains("video") -> {
                    when {
                        mimeType.contains("mp4") -> ".mp4"
                        mimeType.contains("avi") -> ".avi"
                        mimeType.contains("mkv") -> ".mkv"
                        mimeType.contains("mov") -> ".mov"
                        else -> ".mp4"
                    }
                }
                mimeType.contains("audio") -> {
                    when {
                        mimeType.contains("mp3") -> ".mp3"
                        mimeType.contains("wav") -> ".wav"
                        mimeType.contains("aac") -> ".aac"
                        mimeType.contains("flac") -> ".flac"
                        mimeType.contains("ogg") -> ".ogg"
                        else -> ".mp3"
                    }
                }
                mimeType.contains("text") -> {
                    when {
                        mimeType.contains("html") -> ".html"
                        mimeType.contains("xml") -> ".xml"
                        mimeType.contains("json") -> ".json"
                        else -> ".txt"
                    }
                }
                mimeType.contains("excel") ||
                        mimeType.contains("spreadsheetml") ||
                        mimeType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        mimeType.contains("application/vnd.ms-excel") -> ".xlsx"

                mimeType.contains("powerpoint") ||
                        mimeType.contains("presentationml") ||
                        mimeType.contains("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                        mimeType.contains("application/vnd.ms-powerpoint") -> ".pptx"
                else -> ""
            }

            if (extension.isNotEmpty()) {
                fileName = "$fileName$extension"
                Log.d("FileUtils", "Added extension: $fileName")
            }
        }

        if (fileName != null && !fileName!!.contains('.')) {
            fileName = "$fileName.file"
        }

        Log.d("FileUtils", "Final file name: $fileName")
        return fileName ?: "document_${System.currentTimeMillis()}.file"
    }

    fun getFileExtension(contentResolver: ContentResolver, uri: Uri): String {
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        return when {

            mimeType.contains("word") ||
                    mimeType.contains("msword") ||
                    mimeType.contains("wordprocessingml") -> "docx"

            mimeType.contains("pdf") -> "pdf"

            mimeType.contains("image") -> {
                when {
                    mimeType.contains("png") -> "png"
                    mimeType.contains("jpeg") -> "jpg"
                    mimeType.contains("gif") -> "gif"
                    mimeType.contains("bmp") -> "bmp"
                    mimeType.contains("webp") -> "webp"
                    else -> "jpg"
                }
            }

            mimeType.contains("video") -> {
                when {
                    mimeType.contains("mp4") -> "mp4"
                    mimeType.contains("avi") -> "avi"
                    mimeType.contains("mkv") -> "mkv"
                    else -> "mp4"
                }
            }

            mimeType.contains("audio") -> {
                when {
                    mimeType.contains("mp3") -> "mp3"
                    mimeType.contains("wav") -> "wav"
                    mimeType.contains("aac") -> "aac"
                    else -> "mp3"
                }
            }

            mimeType.contains("text") -> "txt"

            mimeType.contains("excel") ||
                    mimeType.contains("spreadsheetml") -> "xlsx"

            mimeType.contains("powerpoint") ||
                    mimeType.contains("presentationml") -> "pptx"
            else -> "file"
        }
    }

    fun getFileExtensionFromPath(path: String): String {
        val ext = File(path).extension
        return if (ext.isNotEmpty() && ext != "file") ext else "file"
    }
}