package e.wallpaper
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.TextureData
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ASTCData(val file: FileHandle) : TextureData {
    private var width = 0
    private var height = 0
    private var data: ByteBuffer? = null
    private var isPrepared = false
    private var internalFormat = 0

    override fun getType(): TextureData.TextureDataType = TextureData.TextureDataType.Custom

    override fun prepare() {
        if (isPrepared) return
        val rawData = file.readBytes()
        val buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN)
        val magic = buffer.int
        if (magic != 0x5CA1AB13) throw GdxRuntimeException("Not a valid ASTC file")
        val blockX = buffer.get().toInt() and 0xFF
        val blockY = buffer.get().toInt() and 0xFF

        width = (buffer.get().toInt() and 0xFF) or ((buffer.get().toInt() and 0xFF) shl 8) or ((buffer.get().toInt() and 0xFF) shl 16)
        height = (buffer.get().toInt() and 0xFF) or ((buffer.get().toInt() and 0xFF) shl 8) or ((buffer.get().toInt() and 0xFF) shl 16)
        internalFormat = when ("$blockX x $blockY") {
            "4 x 4" -> 0x93B0
            "6 x 6" -> 0x93B4
            "8 x 8" -> 0x93B7
            else -> 0x93B0
        }
        buffer.position(16)
        data = buffer.slice()
        isPrepared = true
    }
    override fun consumeCustomData(target: Int) {
        com.badlogic.gdx.Gdx.gl30.glCompressedTexImage2D(
            target, 0, internalFormat, width, height, 0, data!!.remaining(), data
        )
    }

    override fun getWidth(): Int = width
    override fun getHeight(): Int = height
    override fun getFormat(): Pixmap.Format = Pixmap.Format.RGBA8888
    override fun useMipMaps(): Boolean = false
    override fun isPrepared(): Boolean = isPrepared
    override fun isManaged(): Boolean = true
    override fun consumePixmap(): Pixmap = throw GdxRuntimeException("ASTC doesn't use Pixmap")
    override fun disposePixmap(): Boolean = false
}
