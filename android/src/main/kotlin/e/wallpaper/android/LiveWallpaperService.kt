package e.wallpaper.android

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService
import e.wallpaper.Main

class LiveWallpaperService : AndroidLiveWallpaperService() {
    override fun onCreateApplication() {
        super.onCreateApplication()

        val config = AndroidApplicationConfiguration().apply {
            useGL30 = true
            useAccelerometer = true
            disableAudio = true
            useRotationVectorSensor = true
        }
        initialize(Main("extracted/"), config)
    }
}
