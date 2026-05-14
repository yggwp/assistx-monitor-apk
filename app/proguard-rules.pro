# Keep data model classes
-keep class com.assistx.monitor.data.model.** { *; }
-keep class com.assistx.monitor.network.ApiService$** { *; }
-keepclassmembers class com.assistx.monitor.network.ApiService$** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Firebase
-keep class com.google.firebase.** { *; }
