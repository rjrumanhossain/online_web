# Keep JavascriptInterface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.bsd.sunmiprint.JsBridge { *; }
-keep class com.bsd.sunmiprint.SunmiPrinterHelper { *; }

# Sunmi printer library
-keep class com.sunmi.** { *; }
-dontwarn com.sunmi.**
