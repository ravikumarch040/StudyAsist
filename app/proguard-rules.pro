# Add project specific ProGuard rules here.
# Keep Room and POI if needed in release
-keep class * extends androidx.room.RoomDatabase
-dontwarn org.apache.poi.**
