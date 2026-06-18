package com.example.medicaldocuments.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicaldocuments.ui.common.CategoryItem
import com.example.medicaldocuments.ui.common.CategoryManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CategoryManagerDialog(
    onDismiss: () -> Unit,
    onCategoryUpdated: () -> Unit
) {
    var categories by remember {
        mutableStateOf(CategoryManager.getCategories().toMutableList())
    }
    var showAddCategory by remember { mutableStateOf(false) }
    var showEditCategory by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Управление категориями",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                IconButton(
                    onClick = { showAddCategory = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить категорию",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "Всего категорий: ${categories.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(
                        items = categories,
                        key = { category -> category.displayName }
                    ) { category ->
                        CategoryManagerItem(
                            category = category,
                            onEdit = {
                                editingCategory = category
                                showEditCategory = true
                            },
                            onDelete = {
                                categoryToDelete = category
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Закрыть", fontWeight = FontWeight.Medium)
            }
        }
    )

    if (showDeleteConfirmation && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                categoryToDelete = null
            },
            title = { Text("Удалить категорию?") },
            text = {
                Text("Вы уверены, что хотите удалить категорию \"${categoryToDelete?.displayName}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let {
                            CategoryManager.removeCategory(it.displayName)
                            categories = CategoryManager.getCategories().toMutableList()
                            onCategoryUpdated()
                        }
                        showDeleteConfirmation = false
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showAddCategory) {
        AddCategoryDialog(
            onDismiss = { showAddCategory = false },
            onCategoryAdded = { name, iconName, color ->
                CategoryManager.addCategory(name, iconName, color)
                categories = CategoryManager.getCategories().toMutableList()
                onCategoryUpdated()
                showAddCategory = false
            }
        )
    }

    if (showEditCategory && editingCategory != null) {
        EditCategoryDialog(
            category = editingCategory!!,
            onDismiss = {
                showEditCategory = false
                editingCategory = null
            },
            onCategoryEdited = { oldName, newName, iconName, color ->
                CategoryManager.editCategory(oldName, newName, iconName, color)
                categories = CategoryManager.getCategories().toMutableList()
                onCategoryUpdated()
                showEditCategory = false
                editingCategory = null
            }
        )
    }
}

@Composable
fun CategoryManagerItem(
    category: CategoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (category.isCustom)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(category.color).copy(alpha = 0.3f),
                                    Color(category.color).copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        category.getIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(category.color)
                    )
                }
                Column {
                    Text(
                        category.displayName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    if (category.isCustom) {
                        Text(
                            "Пользовательская",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (category.isDefault) {
                        Text(
                            "Стандартная",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCategoryAdded: (String, String, Long) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("Folder") }
    var selectedColor by remember { mutableStateOf(0xFF2196F3) }

    val iconMap = mapOf(
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

    val availableIcons = listOf(
        "Folder" to "Папка",
        "Star" to "Звезда",
        "Science" to "Наука",
        "MedicalInformation" to "Медицина",
        "Description" to "Документ",
        "Receipt" to "Рецепт",
        "Assignment" to "Задание",
        "FolderOpen" to "Открытая папка",
        "Favorite" to "Сердце",
        "HealthAndSafety" to "Здоровье",
        "Medication" to "Лекарство",
        "LocalHospital" to "Больница",
        "Person" to "Человек",
        "Info" to "Информация",
        "Build" to "Инструменты",
        "Settings" to "Настройки",
        "Home" to "Дом",
        "ShoppingCart" to "Корзина",
        "Email" to "Почта",
        "Call" to "Телефон",
        "LocationOn" to "Местоположение",
        "Event" to "Событие",
        "AccessTime" to "Время",
        "DateRange" to "Дата",
        "Warning" to "Предупреждение",
        "Error" to "Ошибка",
        "CheckCircle" to "Успешно"
    )

    val availableColors = listOf(
        0xFF2196F3 to "Синий",
        0xFF4CAF50 to "Зеленый",
        0xFFFF5722 to "Оранжевый",
        0xFF9C27B0 to "Фиолетовый",
        0xFF00BCD4 to "Голубой",
        0xFFFF9800 to "Желтый",
        0xFFE91E63 to "Розовый",
        0xFF795548 to "Коричневый",
        0xFF607D8B to "Серый",
        0xFFF44336 to "Красный",
        0xFF8BC34A to "Салатовый",
        0xFFFFEB3B to "Лимонный",
        0xFF673AB7 to "Индиго",
        0xFF009688 to "Бирюзовый"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Добавить категорию",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Название категории") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    "Выберите иконку:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    items(availableIcons) { (iconName, displayName) ->
                        val icon = iconMap[iconName] ?: Icons.Default.Folder
                        IconButton(
                            onClick = { selectedIconName = iconName },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedIconName == iconName)
                                        Color(selectedColor).copy(alpha = 0.2f)
                                    else
                                        Color.Transparent
                                )
                        ) {
                            Icon(
                                icon,
                                contentDescription = displayName,
                                tint = if (selectedIconName == iconName)
                                    Color(selectedColor)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    "Выберите цвет:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableColors) { (color, name) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selectedColor == color)
                                        Color(color)
                                    else
                                        Color(color).copy(alpha = 0.5f)
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onCategoryAdded(categoryName, selectedIconName, selectedColor)
                    }
                },
                enabled = categoryName.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Добавить", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Отмена", fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun EditCategoryDialog(
    category: CategoryItem,
    onDismiss: () -> Unit,
    onCategoryEdited: (String, String, String, Long) -> Unit
) {
    var categoryName by remember { mutableStateOf(category.displayName) }
    var selectedIconName by remember { mutableStateOf(category.iconName) }
    var selectedColor by remember { mutableStateOf(category.color) }

    val iconMap = mapOf(
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

    val availableIcons = listOf(
        "Folder" to "Папка",
        "Star" to "Звезда",
        "Science" to "Наука",
        "MedicalInformation" to "Медицина",
        "Description" to "Документ",
        "Receipt" to "Рецепт",
        "Assignment" to "Задание",
        "FolderOpen" to "Открытая папка",
        "Favorite" to "Сердце",
        "HealthAndSafety" to "Здоровье",
        "Medication" to "Лекарство",
        "LocalHospital" to "Больница",
        "Person" to "Человек",
        "Info" to "Информация",
        "Build" to "Инструменты",
        "Settings" to "Настройки",
        "Home" to "Дом",
        "ShoppingCart" to "Корзина",
        "Email" to "Почта",
        "Call" to "Телефон",
        "LocationOn" to "Местоположение",
        "Event" to "Событие",
        "AccessTime" to "Время",
        "DateRange" to "Дата",
        "Warning" to "Предупреждение",
        "Error" to "Ошибка",
        "CheckCircle" to "Успешно"
    )

    val availableColors = listOf(
        0xFF2196F3 to "Синий",
        0xFF4CAF50 to "Зеленый",
        0xFFFF5722 to "Оранжевый",
        0xFF9C27B0 to "Фиолетовый",
        0xFF00BCD4 to "Голубой",
        0xFFFF9800 to "Желтый",
        0xFFE91E63 to "Розовый",
        0xFF795548 to "Коричневый",
        0xFF607D8B to "Серый",
        0xFFF44336 to "Красный",
        0xFF8BC34A to "Салатовый",
        0xFFFFEB3B to "Лимонный",
        0xFF673AB7 to "Индиго",
        0xFF009688 to "Бирюзовый"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Редактировать категорию",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Название категории") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    "Выберите иконку:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    items(availableIcons) { (iconName, displayName) ->
                        val icon = iconMap[iconName] ?: Icons.Default.Folder
                        IconButton(
                            onClick = { selectedIconName = iconName },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedIconName == iconName)
                                        Color(selectedColor).copy(alpha = 0.2f)
                                    else
                                        Color.Transparent
                                )
                        ) {
                            Icon(
                                icon,
                                contentDescription = displayName,
                                tint = if (selectedIconName == iconName)
                                    Color(selectedColor)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    "Выберите цвет:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableColors) { (color, name) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selectedColor == color)
                                        Color(color)
                                    else
                                        Color(color).copy(alpha = 0.5f)
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onCategoryEdited(category.displayName, categoryName, selectedIconName, selectedColor)
                    }
                },
                enabled = categoryName.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Отмена", fontWeight = FontWeight.Medium)
            }
        }
    )
}