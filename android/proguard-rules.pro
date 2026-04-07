-verbose
-dontwarn android.support.**
-dontwarn androidx.**
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod, LineNumberTable, SourceFile
-renamesourcefileattribute SourceFile

-keep class com.badlogic.gdx.backends.android.** { *; }
-keep class com.badlogic.gdx.controllers.android.AndroidControllers { *; }

-keep class com.badlogic.gdx.graphics.g2d.Gdx2DPixmap { *; }
-keep class com.badlogic.gdx.jnigen.** { *; }
-keep class com.badlogic.gdx.physics.box2d.** { *; }
-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
    boolean contactFilter(long, long);
    void    beginContact(long);
    void    endContact(long);
    void    preSolve(long, long);
    void    postSolve(long, long);
    boolean reportFixture(long);
    float   reportRayFixture(long, float, float, float, float, float);
}

-keep public class com.badlogic.gdx.scenes.scene2d.** { *; }
-keep public class com.badlogic.gdx.graphics.g2d.BitmapFont { *; }
-keep public class com.badlogic.gdx.graphics.Color { *; }

-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.jnigen.Build
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreeTypeBuild

-keep class com.badlogic.gdx.utils.JsonValue { *; }
-keep class * extends com.badlogic.gdx.ApplicationListener
-keep class * extends com.badlogic.gdx.Game
-keep class * extends com.badlogic.gdx.Screen
-keep class kotlin.Metadata { *; }
-dontwarn groovy.**
-dontwarn org.codehaus.groovy.**
-dontwarn org.apache.ivy.**
-dontwarn com.thoughtworks.xstream.**

-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn javax.annotation.**

-dontwarn edu.umd.cs.findbugs.**
-dontwarn javax.annotation.concurrent.**

-dontwarn kotlin.reflect.**
