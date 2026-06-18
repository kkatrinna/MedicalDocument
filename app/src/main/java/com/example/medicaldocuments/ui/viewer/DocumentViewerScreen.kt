package com.example.medicaldocuments.ui.viewer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.medicaldocuments.data.model.MedicalDocument
import com.example.medicaldocuments.ui.common.DocumentCategory
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    navController: NavController,
    documentId: Long,
    viewModel: DocumentViewerViewModel = hiltViewModel()
) {
    val document by viewModel.document.collectAsState()
    val context = LocalContext.current

    Log.d("Viewer", "=== DocumentViewerScreen OPENED ===")
    Log.d("Viewer", "Document ID: $documentId")

    LaunchedEffect(documentId) {
        Log.d("Viewer", "Loading document with ID: $documentId")
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(document) {
        Log.d("Viewer", "Document state changed: ${document?.fileName ?: "null"}")
    }

    BackHandler {
        navController.navigateUp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        document?.fileName ?: "Просмотр",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    document?.let { doc ->
                        IconButton(
                            onClick = {
                                val file = File(doc.filePath)
                                if (file.exists()) {
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = doc.mimeType
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Поделиться"))
                                    } catch (e: Exception) {
                                        Log.e("Viewer", "Share error: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Поделиться")
                        }
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
        )
    ) { padding ->
        if (document == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка документа...")
                }
            }
        } else {
            val doc = document!!
            val file = File(doc.filePath)

            Log.d("Viewer", "=== ДОКУМЕНТ ЗАГРУЖЕН ===")
            Log.d("Viewer", "Имя: ${doc.fileName}")
            Log.d("Viewer", "Путь: ${file.absolutePath}")
            Log.d("Viewer", "Существует: ${file.exists()}")
            Log.d("Viewer", "Размер: ${file.length()}")
            Log.d("Viewer", "MIME: ${doc.mimeType}")
            Log.d("Viewer", "Расширение: ${file.extension}")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                when {
                    !file.exists() -> {
                        Log.e("Viewer", "File does not exist!")
                        FileNotFoundView()
                    }
                    doc.mimeType.contains("image") ||
                            file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> {
                        Log.d("Viewer", "Showing image viewer")
                        ImageViewer(file, doc.fileName)
                    }
                    doc.mimeType.contains("pdf") || file.extension.lowercase() == "pdf" -> {
                        Log.d("Viewer", "Showing PDF viewer")
                        PDFViewer(file)
                    }
                    doc.mimeType.contains("word") ||
                            doc.mimeType.contains("msword") ||
                            doc.mimeType.contains("wordprocessingml") ||
                            file.extension.lowercase() in listOf("doc", "docx") -> {
                        Log.d("Viewer", "Showing Word viewer")
                        WordViewer(file)
                    }
                    doc.mimeType.contains("video") ||
                            file.extension.lowercase() in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv") -> {
                        Log.d("Viewer", "Showing Video viewer")
                        VideoViewer(file)
                    }
                    doc.mimeType.contains("audio") ||
                            file.extension.lowercase() in listOf("mp3", "wav", "aac", "flac", "ogg") -> {
                        Log.d("Viewer", "Showing Audio viewer")
                        AudioViewer(file)
                    }
                    doc.mimeType.contains("text") ||
                            file.extension.lowercase() in listOf("txt", "log", "csv", "xml", "json", "html", "css", "js", "kt", "java") -> {
                        Log.d("Viewer", "Showing Text viewer")
                        TextViewer(file)
                    }
                    else -> {
                        Log.d("Viewer", "Showing file info")
                        FileInfoView(doc, file)
                    }
                }
            }
        }
    }
}

@Composable
fun FileNotFoundView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Файл не найден",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun WordViewer(file: File) {
    var content by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(file) {
        try {
            val extension = file.extension.lowercase()
            Log.d("Viewer", "Word file extension: $extension")

            val text = when (extension) {
                "doc" -> {
                    Log.d("Viewer", "Reading .doc file with HWPFDocument")
                    FileInputStream(file).use { input ->
                        HWPFDocument(input).use { doc ->
                            doc.text.toString()
                        }
                    }
                }
                "docx", "docm" -> {
                    Log.d("Viewer", "Reading .docx file with XWPFDocument")
                    FileInputStream(file).use { input ->
                        XWPFDocument(input).use { doc ->
                            doc.paragraphs.joinToString("\n") { it.text }
                        }
                    }
                }
                else -> {
                    Log.e("Viewer", "Unsupported extension: $extension")
                    throw Exception("Unsupported format: .$extension")
                }
            }

            if (text.isBlank()) {
                content = listOf("Документ пуст или не содержит текста")
            } else {
                content = text.split("\n").filter { it.isNotBlank() }
            }
            isLoading = false
        } catch (e: Exception) {
            error = "Ошибка при чтении документа: ${e.message}"
            isLoading = false
            Log.e("Viewer", "Word error: ${e.message}")
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка документа...")
                }
            }
        }
        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        error ?: "Ошибка",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/msword")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Открыть документ"))
                            } catch (e: Exception) {
                                Log.e("Viewer", "Open error: ${e.message}")
                            }
                        }
                    ) {
                        Text("Открыть во внешнем приложении")
                    }
                }
            }
        }
        content.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Документ пуст")
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(content) { paragraph ->
                        Text(
                            text = paragraph,
                            fontSize = 15.sp,
                            color = Color.Black,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PDFViewer(file: File) {
    val context = LocalContext.current
    var pageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }

    LaunchedEffect(file) {
        try {
            val bitmaps = mutableListOf<Bitmap>()
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(pfd).use { renderer ->
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(
                        page.width,
                        page.height,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }
            }
            pfd.close()
            pageBitmaps = bitmaps
            isLoading = false
        } catch (e: Exception) {
            error = "Ошибка: ${e.message}"
            isLoading = false
            Log.e("Viewer", "PDF error: ${e.message}")
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка PDF...")
                }
            }
        }
        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        error ?: "Ошибка",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Открыть PDF"))
                            } catch (e: Exception) {
                                Log.e("Viewer", "Open PDF error: ${e.message}")
                            }
                        }
                    ) {
                        Text("Открыть во внешнем приложении")
                    }
                }
            }
        }
        pageBitmaps.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("PDF не содержит страниц")
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                if (pageBitmaps.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPage > 0) {
                                    currentPage--
                                }
                            },
                            enabled = currentPage > 0
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Предыдущая")
                        }
                        Text(
                            "${currentPage + 1} / ${pageBitmaps.size}",
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = {
                                if (currentPage < pageBitmaps.size - 1) {
                                    currentPage++
                                }
                            },
                            enabled = currentPage < pageBitmaps.size - 1
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Следующая")
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Image(
                        bitmap = pageBitmaps[currentPage].asImageBitmap(),
                        contentDescription = "PDF страница ${currentPage + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun VideoViewer(file: File) {
    val context = LocalContext.current
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        setVideoURI(uri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = false
                            start()
                            error = false
                        }
                        setOnErrorListener { _, _, _ ->
                            error = true
                            true
                        }
                    } catch (e: Exception) {
                        error = true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (error) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                    Text(
                        "Не удалось воспроизвести видео",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AudioViewer(file: File) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(file) {
        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                setDataSource(context, uri)
                prepare()
                setOnCompletionListener {
                    isPlaying = false
                }
                setOnErrorListener { _, _, _ ->
                    error = true
                    true
                }
            }
        } catch (e: Exception) {
            error = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Audiotrack,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            file.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error) {
            Text(
                "Не удалось воспроизвести аудио",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            mediaPlayer?.pause()
                        } else {
                            mediaPlayer?.start()
                        }
                        isPlaying = !isPlaying
                    }
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TextViewer(file: File) {
    var content by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(file) {
        try {
            content = file.readText()
        } catch (e: Exception) {
            error = true
            content = "Не удалось прочитать файл"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        if (error) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Не удалось прочитать файл",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            LazyColumn {
                item {
                    Text(
                        text = content,
                        fontSize = 14.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ImageViewer(file: File, fileName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = file,
            contentDescription = fileName,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun FileInfoView(document: MedicalDocument, file: File) {
    val context = LocalContext.current
    val category = DocumentCategory.fromDisplayName(document.category)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val icon = when {
                document.mimeType.contains("pdf") || file.extension.lowercase() == "pdf" -> Icons.Default.PictureAsPdf
                document.mimeType.contains("video") -> Icons.Default.VideoFile
                document.mimeType.contains("audio") -> Icons.Default.Audiotrack
                document.mimeType.contains("text") -> Icons.Default.Description
                else -> category.icon
            }

            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                file.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Тип: ${document.mimeType}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "Размер: ${formatFileSize(file.length())}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, document.mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Открыть"))
                    } catch (e: Exception) {
                        Log.e("Viewer", "Open error: ${e.message}")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Открыть")
            }
        }
    }
}

fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}