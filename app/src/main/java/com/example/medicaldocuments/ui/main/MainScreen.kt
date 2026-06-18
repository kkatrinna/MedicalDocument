package com.example.medicaldocuments.ui.main

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.medicaldocuments.data.model.MedicalDocument
import com.example.medicaldocuments.ui.common.CategoryItem
import com.example.medicaldocuments.ui.common.CategoryManager
import com.example.medicaldocuments.ui.common.DocumentCategory
import com.example.medicaldocuments.ui.theme.AppTheme
import com.example.medicaldocuments.ui.theme.ThemeManager
import com.example.medicaldocuments.ui.theme.ThemeSwitcherDialog
import com.example.medicaldocuments.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCategoryManager by remember { mutableStateOf(false) }
    val categories by CategoryManager.categories.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    val currentTheme by ThemeManager.currentTheme.collectAsState()

    BackHandler(enabled = uiState.selectedCategory != "Все") {
        viewModel.selectCategory("Все")
    }

    val fabScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fab_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "MedDoc",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {},
                actions = {
                    IconButton(
                        onClick = {
                            if (uiState.selectedCategory == "Избранное") {
                                viewModel.selectCategory("Все")
                            } else {
                                viewModel.selectCategory("Избранное")
                            }
                        }
                    ) {
                        Icon(
                            if (uiState.selectedCategory == "Избранное") Icons.Default.Star
                            else Icons.Default.StarBorder,
                            contentDescription = "Избранное",
                            tint = if (uiState.selectedCategory == "Избранное")
                                Color(0xFFFFD700)
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(
                            when (currentTheme) {
                                AppTheme.LIGHT -> Icons.Default.LightMode
                                AppTheme.DARK -> Icons.Default.DarkMode
                                AppTheme.SYSTEM -> Icons.Default.Settings
                            },
                            contentDescription = "Тема"
                        )
                    }
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    }
                    IconButton(onClick = { showCategoryManager = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Управление категориями")
                    }
                    IconButton(onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            viewModel.exportBackup(activity)
                        } else {
                            Toast.makeText(context, "Ошибка: Activity не найдена", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Бэкап")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                windowInsets = WindowInsets.statusBars.add(
                    WindowInsets.safeDrawing
                )
            )
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = uiState.selectedCategory,
                transitionSpec = {
                    fadeIn() + scaleIn() with fadeOut() + scaleOut()
                }
            ) { category ->
                FloatingActionButton(
                    onClick = {
                        val selectedCat = if (category == "Все" || category == "Избранное") {
                            "Анализы"
                        } else {
                            category
                        }
                        navController.navigate("add/$selectedCat")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.scale(fabScale),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить документ",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets.statusBars.add(
            WindowInsets.navigationBars
        )
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            if (uiState.selectedCategory == "Все") {
                AnimatedContent(
                    targetState = categories.isNotEmpty(),
                    transitionSpec = {
                        fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                    }
                ) { hasCategories ->
                    if (hasCategories) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Ваши категории",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Всего документов: ${uiState.categoryCounts["Все"] ?: 0}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                CategoryGridVertical(
                    categories = categories,
                    onCategoryClick = { categoryName ->
                        viewModel.selectCategory(categoryName)
                    },
                    documentCounts = uiState.categoryCounts
                )
            } else {
                AnimatedContent(
                    targetState = uiState.groupedDocuments.isEmpty(),
                    transitionSpec = {
                        fadeIn() + slideInHorizontally() with fadeOut() + slideOutHorizontally()
                    }
                ) { isEmpty ->
                    if (isEmpty) {
                        EmptyStateView(
                            category = uiState.selectedCategory,
                            onAddClick = {
                                navController.navigate("add/${uiState.selectedCategory}")
                            }
                        )
                    } else {
                        DocumentsList(
                            groupedDocuments = uiState.groupedDocuments,
                            onDocumentClick = { document ->
                                navController.navigate("detail/${document.id}")
                            },
                            onFavoriteToggle = viewModel::toggleFavorite,
                            onDelete = { document ->
                                viewModel.deleteDocument(document)
                                Toast.makeText(context, "Документ удален", Toast.LENGTH_SHORT).show()
                            },
                            onBackToCategories = { viewModel.selectCategory("Все") },
                            selectedCategory = uiState.selectedCategory
                        )
                    }
                }
            }
        }
    }

    if (showCategoryManager) {
        CategoryManagerDialog(
            onDismiss = { showCategoryManager = false },
            onCategoryUpdated = { }
        )
    }

    if (showThemeDialog) {
        ThemeSwitcherDialog(
            onDismiss = { showThemeDialog = false },
            onThemeChanged = { }
        )
    }
}


@Composable
fun CategoryGridVertical(
    categories: List<CategoryItem>,
    onCategoryClick: (String) -> Unit,
    documentCounts: Map<String, Int>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = categories,
            key = { category -> category.displayName }
        ) { category ->
            CategoryCardVertical(
                category = category,
                count = documentCounts[category.displayName] ?: 0,
                onClick = { onCategoryClick(category.displayName) }
            )
        }
    }
}

@Composable
fun CategoryCardVertical(
    category: CategoryItem,
    count: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .scale(scale)
            .clickable { onClick() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(category.color).copy(alpha = 0.1f),
                            Color(category.color).copy(alpha = 0.05f)
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(category.color).copy(alpha = 0.3f),
                                Color(category.color).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.getIcon(),
                    contentDescription = category.displayName,
                    modifier = Modifier.size(28.dp),
                    tint = Color(category.color)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(category.color)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$count документов",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(category.color).copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun EmptyStateView(
    category: String,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .scale(1f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                "Нет документов",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "в категории \"$category\"",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить документ")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentsList(
    groupedDocuments: Map<String, List<MedicalDocument>>,
    onDocumentClick: (MedicalDocument) -> Unit,
    onFavoriteToggle: (MedicalDocument) -> Unit,
    onDelete: (MedicalDocument) -> Unit,
    onBackToCategories: () -> Unit,
    selectedCategory: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "Все") "Все документы" else selectedCategory,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${groupedDocuments.values.sumOf { it.size }}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        groupedDocuments.forEach { (year, documents) ->
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = year,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${documents.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            items(
                items = documents,
                key = { it.id }
            ) { document ->
                DocumentItem(
                    document = document,
                    onClick = { onDocumentClick(document) },
                    onFavoriteClick = { onFavoriteToggle(document) },
                    onDelete = { onDelete(document) }
                )
            }
        }
    }
}

@Composable
fun DocumentItem(
    document: MedicalDocument,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDelete: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "doc_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .clickable { onClick() }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val category = DocumentCategory.fromDisplayName(document.category)

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(category.color).copy(alpha = 0.25f),
                                Color(category.color).copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color(category.color)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = document.fileName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(category.color).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = document.category,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(category.color),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = DateUtils.formatDate(document.documentDate),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (document.comment != null) {
                        Text(
                            text = "• ${document.comment}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    if (document.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Избранное",
                    tint = if (document.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}