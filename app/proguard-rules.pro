# Add project specific ProGuard rules here.

# Keep TermuxInstaller
-keep class ai.openclaw.android.installer.** { *; }

# Keep Service
-keep class ai.openclaw.android.service.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
