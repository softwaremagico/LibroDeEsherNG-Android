# Keep the LibroDeEsher rule model: Jackson deserializes it reflectively.
-keep class com.softwaremagico.librodeesher.** { *; }

# Jackson
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn com.fasterxml.jackson.**

# StAX / Woodstox
-dontwarn javax.xml.stream.**
-dontwarn org.codehaus.stax2.**

# SLF4J
-dontwarn org.slf4j.**
