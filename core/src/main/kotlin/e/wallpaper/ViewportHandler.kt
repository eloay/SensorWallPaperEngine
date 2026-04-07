package e.wallpaper

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * 专门负责计算渲染位置和尺寸的工具类
 */
class ViewportHandler {

    // 缓存计算结果，避免每帧创建新对象
    private var drawX = 0f
    private var drawY = 0f
    private var drawWidth = 0f
    private var drawHeight = 0f

    /**
     * 根据屏幕尺寸和纹理尺寸，计算 Center Crop 后的参数并绘制
     */
    fun drawCenterCrop(batch: SpriteBatch, texture: Texture, screenWidth: Float, screenHeight: Float) {
        val imgWidth = texture.width.toFloat()
        val imgHeight = texture.height.toFloat()

        // 1. 计算填充屏幕所需的最小缩放比例
        val scale = (screenWidth / imgWidth).coerceAtLeast(screenHeight / imgHeight)

        // 2. 计算放大后的尺寸
        drawWidth = imgWidth * scale
        drawHeight = imgHeight * scale

        // 3. 计算居中偏移量
        drawX = (screenWidth - drawWidth) / 2f
        drawY = (screenHeight - drawHeight) / 2f

        // 4. 执行绘制
        batch.draw(texture, drawX, drawY, drawWidth, drawHeight)
    }
}
