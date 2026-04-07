package e.wallpaper

object WallpaperConfig {
    var lerpFactor = 0.08f
    var deadzone = 0.15f
    var maxSensitivity = 4.0f
    var loadRadius = 8

    fun update(lerp: Float, dead: Float, sens: Float, radius: Int) {
        this.lerpFactor = lerp
        this.deadzone = dead
        this.maxSensitivity = sens
        this.loadRadius = radius
    }
}
