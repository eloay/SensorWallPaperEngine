package e.wallpaper.android


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import e.wallpaper.WallpaperConfig
import androidx.core.content.edit

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("wallpaper_settings", MODE_PRIVATE)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(prefs)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(prefs: android.content.SharedPreferences) {
    var lerp by remember { mutableFloatStateOf(prefs.getFloat("lerp_factor", 0.08f)) }
    var sens by remember { mutableFloatStateOf(prefs.getFloat("max_sensitivity", 4.0f)) }
    var dead by remember { mutableFloatStateOf(prefs.getFloat("dead_zone", 0.15f)) }

    Column(modifier = Modifier.padding(30.dp,16.dp,30.dp,16.dp).fillMaxWidth()) {
        Text("壁纸设置", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // 灵敏度设置
        SettingSlider("响应速度 (Max Sensitivity)", sens, 1f..10f) {
            sens = it
            prefs.edit { putFloat("max_sensitivity", it) }
            WallpaperConfig.maxSensitivity = it
        }

        // 平滑度设置
        SettingSlider("丝滑程度 (Lerp Factor)", lerp, 0.01f..0.3f) {
            lerp = it
            prefs.edit { putFloat("lerp_factor", it) }
            WallpaperConfig.lerpFactor = it
        }

        // 死区设置
        SettingSlider("防抖阈值 (Deadzone)", dead, 0f..0.5f) {
            dead = it
            prefs.edit().putFloat("dead_zone", it).apply()
            WallpaperConfig.deadzone = it
        }

        Text("提示：调整后返回桌面即可生效", color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun SettingSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("$label: ${"%.2f".format(value)}")
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
