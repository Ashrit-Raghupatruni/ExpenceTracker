# ProGuard & R8 configuration for ShakeExpense

# Keep Room database entities & DAOs
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep Firebase & Google Auth models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep DTOs & Models
-keep class com.shakeexpense.app.sync.model.** { *; }
-keep class com.shakeexpense.app.domain.model.** { *; }
-keep class com.shakeexpense.app.data.database.entity.** { *; }
