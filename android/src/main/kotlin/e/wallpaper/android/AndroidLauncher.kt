package e.wallpaper.android

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.edit
import e.wallpaper.WallpaperConfig
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import kotlinx.coroutines.launch

class AndroidLauncher : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    WallpaperManagerScreen()
                }
            }
        }
        val prefs = getSharedPreferences("wallpaper_settings", MODE_PRIVATE)
        WallpaperConfig.update(
            prefs.getFloat("lerp_factor", 0.08f),
            prefs.getFloat("dead_zone", 0.15f),
            prefs.getFloat("max_sensitivity", 4.0f),
            prefs.getInt("load_radius", 8)
        )
    }
}

@Composable
fun WallpaperManagerScreen() {
    val context = LocalContext.current
    var fileName by remember { mutableStateOf("正在加载...") }

    LaunchedEffect(Unit) {
        fileName = getLastFileName(context)
    }
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploading = true // 显示进度条
                val savedName = saveZipToInternalStorage(context, it)
                if (savedName != null) {
                    fileName = savedName
                }
                isUploading = false // 关闭进度条
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        IconButton(
            onClick = {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "参数设置",
                tint = MaterialTheme.colors.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center) // 保持原有内容居中
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "动态壁纸管理",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "当前选中的ASTC包：",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = fileName, fontSize = 14.sp)
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (!isUploading) {
                            filePickerLauncher.launch("application/zip")
                        }
                    },
                    enabled = !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colors.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("更换 ASTC 包")
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                            putExtra(
                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                ComponentName(context, "e.wallpaper.android.LiveWallpaperService")
                            )
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.secondary)
                ) {
                    Text("前往系统壁纸设置")
                }
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
suspend fun saveZipToInternalStorage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
    val fileName = getFileName(context, uri) ?: "wallpaper.zip"
    // 原始 ZIP 路径
    val zipFile = File(context.filesDir, "current_wallpaper.zip")
    // 解压目标目录
    val extractedDir = File(context.filesDir, "extracted")

    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(zipFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        if (extractedDir.exists()) {
            extractedDir.deleteRecursively() // 删除旧文件夹及其所有内容
        }
        extractedDir.mkdirs()
        // --- 解压 ---
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(extractedDir, entry.name)

                if (!newFile.canonicalPath.startsWith(extractedDir.canonicalPath)) {
                    throw SecurityException("ZIP entry tried to escape destination directory")
                }

                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        // --- 记录文件名 ---
        val prefs = context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("last_file_name", fileName) }

        return@withContext fileName
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }finally {
        if (zipFile.exists()) {
            zipFile.delete()
        }
    }
}

fun getLastFileName(context: Context): String {
    val prefs = context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
    return prefs.getString("last_file_name", "未选择文件") ?: "未选择文件"
}
