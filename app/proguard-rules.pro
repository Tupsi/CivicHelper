# Project specific ProGuard rules
# ---------------------------------------------------------------------------

# 1. Erhalte Zeilennummern für aussagekräftige Crash-Reports in der Play Console
# Dies ist entscheidend, damit die mapping.txt Datei Abstürze korrekt zuordnen kann.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. Schütze deine Datenbank-Modelle und DAOs
# R8 soll die Feldnamen und Klassen in deinem DB-Paket nicht umbenennen,
# um maximale Kompatibilität mit Room und Typ-Konvertern zu garantieren.
-keep class org.tesira.civic.db.** { *; }

# 3. Room Database spezifisch
# Stellt sicher, dass die generierten Implementierungen deiner Datenbank gefunden werden.
-keep class * extends androidx.room.RoomDatabase
-keepnames class androidx.room.RoomDatabase

# 4. Verhindere Warnungen von Drittanbieter-Bibliotheken
# Manche Bibliotheken haben Abhängigkeiten, die zur Laufzeit nicht gebraucht werden.
-dontwarn it.xabaras.android.**

# 5. ViewModel Schutz
# Stellt sicher, dass die Konstruktoren deiner ViewModels für die Factory erhalten bleiben.
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
