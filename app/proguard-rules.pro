# ------------------------------------------------------------------------------------
# ProGuard & R8 Obfuscation and Shrinking Rules for GluedOTT
# ------------------------------------------------------------------------------------

# Keep general Java and Kotlin reflection metadata
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------------------------
# Data Transfer Objects (DTOs) & Gson
# ------------------------------------------------------------------------------------
# Prevent Gson reflection failures by preserving annotated fields and DTO models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

-keep class com.ott.gluedin.data.dto.** { *; }
-keep class com.ott.gluedin.domain.entity.** { *; }

-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }

# ------------------------------------------------------------------------------------
# AndroidX Media3 / ExoPlayer
# ------------------------------------------------------------------------------------
# Retain codec discovery, renderers, and UI components used dynamically
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.datasource.** { *; }
-dontwarn androidx.media3.**

# ------------------------------------------------------------------------------------
# Custom Views & View Binding
# ------------------------------------------------------------------------------------
# Keep custom views referenced in layout XML files (like TopCropImageView)
-keep public class com.ott.gluedin.presentation.common.** { *; }
-keep class com.ott.gluedin.databinding.** { *; }

-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ------------------------------------------------------------------------------------
# Android Architecture Components & Coroutines
# ------------------------------------------------------------------------------------
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ------------------------------------------------------------------------------------
# Coil Image Loader
# ------------------------------------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

# ------------------------------------------------------------------------------------
# Parcelable Implementation
# ------------------------------------------------------------------------------------
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ------------------------------------------------------------------------------------
# Dagger & Hilt
# ------------------------------------------------------------------------------------
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class com.ott.gluedin.di.** { *; }

