# Keep line numbers for readable Crashlytics stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization, Retrofit 3, OkHttp 5, Okio, Coil, Hilt and AndroidX ship their own consumer
# rules (r8-analyzer: the previous package-wide / interface-wide copies were redundant and are removed).

# ---- Jsoup ----------------------------------------------------------------
-dontwarn org.jspecify.annotations.**
-dontwarn com.google.re2j.**

# ---- Navigation 3 ---------------------------------------------------------
# The back stack is saved/restored polymorphically by serial name (= class name), so the NavKey
# classes must keep their names (narrow rule: names only, members/shrinking unaffected).
-keepnames class com.futabooo.android.booklife.ui.navigation.** implements androidx.navigation3.runtime.NavKey
