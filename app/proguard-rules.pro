# Keep line numbers for readable Crashlytics stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- kotlinx.serialization -------------------------------------------------
# (the library ships consumer rules; these cover our @Serializable models/NavKeys)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.futabooo.android.booklife.**$$serializer { *; }
-keepclassmembers class com.futabooo.android.booklife.** {
    *** Companion;
}
-keepclasseswithmembers class com.futabooo.android.booklife.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Retrofit 3 / OkHttp 5 / Okio -----------------------------------------
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keep,allowobfuscation interface com.futabooo.android.booklife.data.network.BookmeterApi
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Jsoup ----------------------------------------------------------------
-dontwarn org.jspecify.annotations.**
-dontwarn com.google.re2j.**

# ---- Coil 3 / Hilt / Navigation 3 -----------------------------------------
# Coil, Hilt and AndroidX ship their own consumer rules.
# NavKeys are serialized by kotlinx.serialization (covered above).

# Navigation 3 back stack is saved/restored polymorphically by serial name (= class name).
-keepnames class com.futabooo.android.booklife.ui.navigation.** implements androidx.navigation3.runtime.NavKey
