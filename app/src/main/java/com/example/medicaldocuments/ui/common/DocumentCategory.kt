package com.example.medicaldocuments.ui.common

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class CategoryItem(
    val displayName: String,
    val iconName: String,
    val color: Long,
    val isCustom: Boolean = false,
    val isDefault: Boolean = false
) {
    fun getIcon(): ImageVector {
        return iconMap[iconName] ?: Icons.Default.Folder
    }

    companion object {
        private val iconMap = mapOf(
            "Folder" to Icons.Default.Folder,
            "Star" to Icons.Default.Star,
            "Science" to Icons.Default.Science,
            "MedicalInformation" to Icons.Default.MedicalInformation,
            "Description" to Icons.Default.Description,
            "Receipt" to Icons.Default.Receipt,
            "Assignment" to Icons.Default.Assignment,
            "FolderOpen" to Icons.Default.FolderOpen,
            "Favorite" to Icons.Default.Favorite,
            "HealthAndSafety" to Icons.Default.HealthAndSafety,
            "Medication" to Icons.Default.Medication,
            "LocalHospital" to Icons.Default.LocalHospital,
            "Person" to Icons.Default.Person,
            "Info" to Icons.Default.Info,
            "Build" to Icons.Default.Build,
            "Settings" to Icons.Default.Settings,
            "Home" to Icons.Default.Home,
            "ShoppingCart" to Icons.Default.ShoppingCart,
            "Email" to Icons.Default.Email,
            "Call" to Icons.Default.Call,
            "LocationOn" to Icons.Default.LocationOn,
            "Event" to Icons.Default.Event,
            "AccessTime" to Icons.Default.AccessTime,
            "DateRange" to Icons.Default.DateRange,
            "Warning" to Icons.Default.Warning,
            "Error" to Icons.Default.Error,
            "CheckCircle" to Icons.Default.CheckCircle
        )

        fun fromCategoryItem(item: CategoryItem): CategoryItem {
            return item
        }
    }
}

object CategoryManager {
    private const val PREFS_NAME = "category_prefs"
    private const val KEY_CATEGORIES = "custom_categories"
    private const val KEY_REMOVED_DEFAULTS = "removed_defaults"

    private lateinit var prefs: SharedPreferences
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val defaultCategories = listOf(
        CategoryItem("Анализы", "Science", 0xFF4CAF50, isDefault = true),
        CategoryItem("Рентген", "MedicalInformation", 0xFFFF5722, isDefault = true),
        CategoryItem("Выписки", "Description", 0xFF9C27B0, isDefault = true),
        CategoryItem("Рецепты", "Receipt", 0xFF00BCD4, isDefault = true),
        CategoryItem("Справки", "Assignment", 0xFFFF9800, isDefault = true),
        CategoryItem("Другое", "FolderOpen", 0xFF607D8B, isDefault = true)
    )

    private var customCategories = mutableListOf<CategoryItem>()
    private var removedDefaultNames = mutableSetOf<String>()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadCategories()
        updateCategories()
    }

    private fun loadCategories() {
        val customJson = prefs.getString(KEY_CATEGORIES, null)
        customCategories = if (customJson != null) {
            try {
                json.decodeFromString(customJson)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        val removedJson = prefs.getString(KEY_REMOVED_DEFAULTS, null)
        removedDefaultNames = if (removedJson != null) {
            try {
                json.decodeFromString(removedJson)
            } catch (e: Exception) {
                mutableSetOf()
            }
        } else {
            mutableSetOf()
        }
    }

    private fun saveCategories() {
        val customJson = json.encodeToString(customCategories)
        prefs.edit().putString(KEY_CATEGORIES, customJson).apply()

        val removedJson = json.encodeToString(removedDefaultNames)
        prefs.edit().putString(KEY_REMOVED_DEFAULTS, removedJson).apply()
    }

    private fun getAvailableDefaults(): List<CategoryItem> {
        return defaultCategories.filter { !removedDefaultNames.contains(it.displayName) }
    }

    private fun updateCategories() {
        val availableDefaults = getAvailableDefaults()
        _categories.value = availableDefaults + customCategories
    }

    fun getCategories(): List<CategoryItem> {
        return _categories.value
    }

    fun addCategory(name: String, iconName: String, color: Long) {
        customCategories.add(CategoryItem(name, iconName, color, isCustom = true))
        saveCategories()
        updateCategories()
    }

    fun removeCategory(name: String) {
        val isDefault = defaultCategories.any { it.displayName == name && it.isDefault }

        if (isDefault) {
            removedDefaultNames.add(name)
        } else {
            customCategories.removeAll { it.displayName == name && it.isCustom }
        }

        saveCategories()
        updateCategories()
    }

    fun editCategory(oldName: String, newName: String, iconName: String, color: Long) {

        val customIndex = customCategories.indexOfFirst { it.displayName == oldName && it.isCustom }
        if (customIndex != -1) {
            customCategories[customIndex] = CategoryItem(newName, iconName, color, isCustom = true)
        } else {
            val isDefault = defaultCategories.any { it.displayName == oldName && it.isDefault }
            if (isDefault) {
                removedDefaultNames.add(oldName)
                customCategories.add(CategoryItem(newName, iconName, color, isCustom = true))
            }
        }
        saveCategories()
        updateCategories()
    }

    fun isCustomCategory(name: String): Boolean {
        return customCategories.any { it.displayName == name && it.isCustom }
    }

    fun getCategoryByName(name: String): CategoryItem? {
        return _categories.value.find { it.displayName == name }
    }

    fun getAllCategoryNames(): List<String> {
        return _categories.value.map { it.displayName }
    }
}

enum class DocumentCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Long
) {
    ALL("Все", Icons.Default.Folder, 0xFF2196F3),
    FAVORITES("Избранное", Icons.Default.Star, 0xFFFFC107),
    TESTS("Анализы", Icons.Default.Science, 0xFF4CAF50),
    XRAY("Рентген", Icons.Default.MedicalInformation, 0xFFFF5722),
    DISCHARGE("Выписки", Icons.Default.Description, 0xFF9C27B0),
    PRESCRIPTIONS("Рецепты", Icons.Default.Receipt, 0xFF00BCD4),
    CERTIFICATES("Справки", Icons.Default.Assignment, 0xFFFF9800),
    OTHER("Другое", Icons.Default.FolderOpen, 0xFF607D8B);

    companion object {
        fun fromDisplayName(displayName: String): DocumentCategory {
            return values().find { it.displayName == displayName } ?: OTHER
        }

        fun getCategoriesForDisplay(): List<DocumentCategory> {
            return values().filter { it != ALL && it != FAVORITES }
        }

        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
}