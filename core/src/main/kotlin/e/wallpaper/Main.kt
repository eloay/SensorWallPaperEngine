package e.wallpaper

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import kotlin.math.abs

class Main(private val folderPath: String = "extracted/") : ApplicationAdapter() {

    private lateinit var motionController: MotionFrameController
    private lateinit var batch: SpriteBatch
    private lateinit var manager: AssetManager
    private val allFilePaths = mutableListOf<String>()
    private var viewportHandler: ViewportHandler? = null
    // --- 内存控制核心常量 ---
    private val LOAD_RADIUS = 20    // 当前帧前后各加载8张，共17张（显存安全线）
    private val UNLOAD_RADIUS = 30 // 超过这个距离的立即卸载

    private var lastCenterIndex = -1
    private var lastRenderedIndex = 0
    override fun create() {
        viewportHandler = ViewportHandler()
        val resolver = LocalFileHandleResolver()
        manager = AssetManager(resolver)

        manager.setLoader(Texture::class.java, ".astc", ASTCLoader(resolver))
        manager.setLoader(Texture::class.java, ".ktx", ASTCLoader(resolver))

        batch = SpriteBatch()
        loadFileList()

        if (allFilePaths.isNotEmpty()) {
            motionController = MotionFrameController(allFilePaths.size)
            manager.load(allFilePaths[0], Texture::class.java)
            manager.finishLoading()
        }
    }

    private fun loadFileList() {
        allFilePaths.clear()
        val dir = Gdx.files.local(folderPath)
        if (dir.exists() && dir.isDirectory) {
            val files = dir.list { _, name ->
                val lower = name.lowercase()
                lower.endsWith(".ktx") || lower.endsWith(".astc")
            }
            if (files.isNotEmpty()) {
                files.sortBy { it.name() }
                files.forEach { allFilePaths.add(it.path()) }
                Gdx.app.log("Main", "Total Frames: ${allFilePaths.size}")
            }
        }
    }
    private var debugCounter = 0

    override fun render() {
        val accelX = Gdx.input.accelerometerX
        val accelY = Gdx.input.accelerometerY
        val accelZ = Gdx.input.accelerometerZ

        debugCounter++
        if (debugCounter % 60 == 0) {

            Gdx.app.log("SensorDebug", "Raw Accel -> X: ${"%.2f".format(accelX)}, Y: ${"%.2f".format(accelY)}, Z: ${"%.2f".format(accelZ)}")
            if (::motionController.isInitialized) {

                Gdx.app.log("SensorDebug", "Target Index: ${motionController.currentFrameIndex} / ${allFilePaths.size}")
            } else {
                Gdx.app.log("SensorDebug", "MotionController NOT initialized!")
            }
        }
        if (allFilePaths.isEmpty()) return

        motionController.update()
        val targetIndex = motionController.currentFrameIndex

        manager.update(16)

        // 动态调整内存窗口
        if (abs(targetIndex - lastCenterIndex) >= 1) {
            updateResources(targetIndex)
            lastCenterIndex = targetIndex
        }

        // 绘图逻辑
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        val currentPath = allFilePaths[targetIndex]
        val tex = if (manager.isLoaded(currentPath)) {
            manager.get<Texture>(currentPath)
        } else {
            findNearestLoadedTexture(targetIndex)
        }

        tex?.let {
            viewportHandler?.drawCenterCrop(
                batch,
                it,
                Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat()
            )

            if (manager.isLoaded(currentPath)) lastRenderedIndex = targetIndex
        }

        batch.end()
    }

    private fun updateResources(centerIndex: Int) {
        val start = (centerIndex - LOAD_RADIUS).coerceAtLeast(0)
        val end = (centerIndex + LOAD_RADIUS).coerceAtMost(allFilePaths.size - 1)

        for (i in start..end) {
            val path = allFilePaths[i]
            if (!manager.contains(path)) {
                manager.load(path, Texture::class.java)
            }
        }

        val loadedPaths = manager.assetNames
        for (path in loadedPaths) {
            if (path == allFilePaths[0]) continue

            val idx = allFilePaths.indexOf(path)
            if (idx != -1 && (idx < centerIndex - UNLOAD_RADIUS || idx > centerIndex + UNLOAD_RADIUS)) {
                manager.unload(path)
            }
        }
    }

    private fun findNearestLoadedTexture(targetIndex: Int): Texture? {
        if (manager.isLoaded(allFilePaths[lastRenderedIndex])) {
            return manager.get(allFilePaths[lastRenderedIndex])
        }
        for (i in 1..10) {
            val l = (targetIndex - i).coerceAtLeast(0)
            if (manager.isLoaded(allFilePaths[l])) return manager.get(allFilePaths[l])
            val r = (targetIndex + i).coerceAtMost(allFilePaths.size - 1)
            if (manager.isLoaded(allFilePaths[r])) return manager.get(allFilePaths[r])
        }
        return if (manager.isLoaded(allFilePaths[0])) manager.get(allFilePaths[0]) else null
    }

    override fun dispose() {
        batch.dispose()
        manager.dispose()
    }
    override fun resume() {
        super.resume()
        if (::motionController.isInitialized) {
            motionController.resetCalibration()
        }
    }
}
