package e.wallpaper
import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
class ASTCLoader(resolver: FileHandleResolver) :
    AsynchronousAssetLoader<Texture, ASTCLoader.ASTCParameters>(resolver) {

    private var data: ASTCData? = null

    override fun loadAsync(
        manager: AssetManager?,
        fileName: String?,
        file: FileHandle?,
        parameter: ASTCParameters?
    ) {
        file?.let {
            data = ASTCData(it)
            data?.prepare()
        }
    }

    override fun loadSync(
        manager: AssetManager?,
        fileName: String?,
        file: FileHandle?,
        parameter: ASTCParameters?
    ): Texture {
        val texture = Texture(data)
        data = null
        return texture
    }

    override fun getDependencies(
        fileName: String?,
        file: FileHandle?,
        parameter: ASTCParameters?
    ): Array<AssetDescriptor<*>>? {
        return null
    }

    class ASTCParameters : AssetLoaderParameters<Texture>()
}
