# Proguard / R8 Optimization Rules for "Search For My Car"

# Preserve Firestore Data Models & Reflection
-keepclassmembers class com.muhanad.fteshlyar.data.** { *; }
-keep class com.muhanad.fteshlyar.data.** { *; }

# Preserve Firebase FCM Messaging Service
-keep class com.muhanad.fteshlyar.service.** { *; }

# Optimize Compose and Coroutines
-dontwarn kotlinx.coroutines.**
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
