# Branch looks up aaid via refelection
-keep class com.google.android.gms.** { *; }

# for Huawei devices without GMS (Branch looks up oaid via refelection)
#-keep class com.huawei.hms.ads.** { *; }
#-keep interface com.huawei.hms.ads.** { *; }