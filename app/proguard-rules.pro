# Keep PDFBox classes used through reflection.
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# Tapsell ships its own consumer rules. Keep this as a safety net.
-keep class ir.tapsell.** { *; }
-dontwarn ir.tapsell.**

# Keep model classes serialized by the app.
-keep class ir.amiraligrr.damdast.data.** { *; }
