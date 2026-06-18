package com.example.medicaldocuments

import android.app.Application
import com.example.medicaldocuments.ui.common.CategoryManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MedicalDocumentsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CategoryManager.initialize(this)
    }
}