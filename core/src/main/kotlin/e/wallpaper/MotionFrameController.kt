package e.wallpaper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import kotlin.math.abs

class MotionFrameController(private val totalFrames: Int) {

    var currentFrameIndex: Int = 0
        private set

    private var smoothedX = 0f
    private var centerOffset = 0f

    // --- 配置参数 ---
    private val lerpFactor get() = WallpaperConfig.lerpFactor
    private val maxSensitivity get() = WallpaperConfig.maxSensitivity
    // 用户可选死区：建议范围 0 到 5（代表帧数差）
    private val frameDeadzone get() = WallpaperConfig.deadzone

    fun update() {
        if (totalFrames <= 0) return

        val rawX = Gdx.input.accelerometerX

        // 平滑处理
        smoothedX += (rawX - smoothedX) * lerpFactor

        // 动态窗口偏移
        if (smoothedX > centerOffset + maxSensitivity) {
            centerOffset = smoothedX - maxSensitivity
        } else if (smoothedX < centerOffset - maxSensitivity) {
            centerOffset = smoothedX + maxSensitivity
        }

        val normalized = (smoothedX - (centerOffset - maxSensitivity)) / (maxSensitivity * 2f)
        val targetIndex = (MathUtils.clamp(normalized, 0f, 1.0f) * (totalFrames - 1)).toInt()

        //死区消抖
        if (abs(targetIndex - currentFrameIndex) > frameDeadzone) {
            currentFrameIndex = targetIndex
        }
    }

    fun resetCalibration() {
        smoothedX = Gdx.input.accelerometerX
        centerOffset = smoothedX
        // 重置索引以防画面瞬间跳跃
        val normalized = 0.5f
        currentFrameIndex = (normalized * (totalFrames - 1)).toInt()
    }
}
