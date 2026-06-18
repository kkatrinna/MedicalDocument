package com.example.medicaldocuments.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.medicaldocuments.ui.common.CategoryManager
import com.example.medicaldocuments.utils.DateUtils
import com.example.medicaldocuments.utils.FileUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    navController: NavController,
    initialCategory: String = "Анализы",
    viewModel: AddDocumentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var documentDate by remember { mutableStateOf(DateUtils.getCurrentTimestamp()) }
    var comment by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    var dateText by remember { mutableStateOf("") }
    var isDateValid by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    LaunchedEffect(documentDate) {
        dateText = try {
            dateFormatter.format(Date(documentDate))
        } catch (e: Exception) {
            ""
        }
        isDateValid = true
    }

    val expanded by viewModel.expanded.collectAsState()
    val categories by CategoryManager.categories.collectAsState()
    val categoryNames = categories.map { it.displayName }

    BackHandler {
        navController.navigateUp()
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            fileName = FileUtils.getFileName(context.contentResolver, it) ?: "Документ"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true ||
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true) {
            pickFileLauncher.launch("*/*")
        } else {
            Toast.makeText(context, "Нужно разрешение для чтения файлов", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Новый документ",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedUri = null
                            selectedCategory = initialCategory
                            documentDate = DateUtils.getCurrentTimestamp()
                            dateText = ""
                            comment = ""
                            fileName = ""
                            isDateValid = true
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Сбросить"
                        )
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
        contentWindowInsets = WindowInsets.statusBars.add(
            WindowInsets.navigationBars
        ),
        modifier = Modifier.imePadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .clickable { focusManager.clearFocus() },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        focusManager.clearFocus()
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }

                        val hasPermission = permissions.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }

                        if (hasPermission) {
                            pickFileLauncher.launch("*/*")
                        } else {
                            permissionLauncher.launch(permissions)
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUri != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedUri != null)
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4CAF50).copy(alpha = 0.3f),
                                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        )
                                    )
                                else
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (selectedUri != null) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (selectedUri != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (selectedUri != null) "Файл выбран" else "Выберите файл",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (selectedUri != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedUri != null) fileName else "Нажмите для выбора документа",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    if (selectedUri != null) {
                        IconButton(onClick = {
                            selectedUri = null
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Удалить файл",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    viewModel.toggleExpanded()
                    focusManager.clearFocus()
                }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { viewModel.toggleExpanded() }
                ) {
                    categoryNames.forEach { categoryName ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    categoryName,
                                    fontWeight = if (categoryName == selectedCategory) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedCategory = categoryName
                                viewModel.toggleExpanded()
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = dateText,
                onValueChange = {
                    val filtered = it.filter { char ->
                        char.isDigit() || char == '.'
                    }
                    dateText = filtered

                    if (filtered.length == 10) {
                        isDateValid = try {
                            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            sdf.isLenient = false
                            val date = sdf.parse(filtered)
                            date?.let {
                                documentDate = it.time
                            }
                            date != null
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        isDateValid = true
                    }
                },
                label = { Text("Дата документа") },
                placeholder = { Text("ДД.ММ.ГГГГ") },
                leadingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isDateValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            showDatePicker = true
                        }
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Выбрать дату",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDateValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    unfocusedBorderColor = if (isDateValid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error,
                    focusedLabelColor = if (isDateValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    unfocusedLabelColor = if (isDateValid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                ),
                supportingText = {
                    if (!isDateValid && dateText.length == 10) {
                        Text(
                            text = "Неверная дата. Используйте ДД.ММ.ГГГГ",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "Нажмите на календарь для выбора даты или введите вручную",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                isError = !isDateValid && dateText.length == 10,
                maxLines = 1,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Комментарий (опционально)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(Int.MAX_VALUE)
                            }
                        }
                    },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedUri == null) {
                        Toast.makeText(context, "Выберите файл", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!isDateValid || dateText.length != 10) {
                        Toast.makeText(context, "Введите корректную дату", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    focusManager.clearFocus()
                    viewModel.saveDocument(
                        uri = selectedUri!!,
                        category = selectedCategory,
                        documentDate = documentDate,
                        comment = comment.takeIf { it.isNotBlank() },
                        fileName = fileName
                    ) { success ->
                        isSaving = false
                        if (success) {
                            Toast.makeText(context, "Документ сохранен", Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        } else {
                            Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving && isDateValid && dateText.length == 10 && selectedUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Text(
                            "Сохранить документ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val currentDate = if (documentDate > 0) {
            Date(documentDate)
        } else {
            Date()
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                documentDate = calendar.timeInMillis
                dateText = dateFormatter.format(calendar.time)
                isDateValid = true
                showDatePicker = false
            },
            initialDate = currentDate
        )
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    initialDate: Date = Date()
) {
    val calendar = Calendar.getInstance().apply {
        time = initialDate
    }

    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                "Выберите дату",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time

                Text(
                    text = dateFormatter.format(selectedDate),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "День",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        NumberPicker(
                            value = selectedDay,
                            onValueChange = { selectedDay = it },
                            range = 1..31,
                            modifier = Modifier.width(72.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Месяц",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        NumberPicker(
                            value = selectedMonth + 1,
                            onValueChange = { selectedMonth = it - 1 },
                            range = 1..12,
                            modifier = Modifier.width(72.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Год",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        NumberPicker(
                            value = selectedYear,
                            onValueChange = { selectedYear = it },
                            range = 1900..2100,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(selectedYear, selectedMonth, selectedDay)
                }
            ) {
                Text("Выбрать", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = {
                if (value < range.last) {
                    onValueChange(value + 1)
                }
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Увеличить",
                tint = if (value < range.last)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }

        Box(
            modifier = Modifier
                .size(60.dp, 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = {
                if (value > range.first) {
                    onValueChange(value - 1)
                }
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Уменьшить",
                tint = if (value > range.first)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}