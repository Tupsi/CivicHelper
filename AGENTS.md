# My AGENTS.md für Civic Helper
- Die Hauptaktivität ist /app/src/main/kotlin/MainActivity.kt
- Der Namespace der App ist org.tesira.civic
- Die App ist ein Hilfsmittel für die Brettspiele "Mega Civilization", "Mega Empires - The West" und "Mega Empires - The East"
- Die Anleitung für das Brettspiel findest du hier: https://tesira.org/MegaCivilization_Rulebook.pdf
- Wir unterhalten uns in deutscher Sprache. Android Studio und die App selbst nutzt die englische Sprache.
- Wenn ich dir Fehler aus Android Studio sende, dann sind die auch in englischer Sprache. Du antwortest mir trotzdem auf deutsch.
- Die App besteht aus den Hauptmenupunkten Home, Buying, Inventory, Library, Tips und Settings.
- Home (HomeFragment.kt und fragment_home.xml) ist die zentrale Seite der App. Sie ist ein Dashboard und zeigt den aktuellen Spielverlauf. Hier wird der Bonus der bereits gekauften Karten angezeigt, 
  sowie die Anzahl der Städte auf dem Spiebrett und die aktuelle Zeit in der sich der Spieler befindet. 
  Daraus werden Siegpunkte (Victory Points) errechnet und angezeigt.
- Buying (BuyingFragment.xml und fragment_buying.xml) gibt die Möglichkeit neue Karten zu kaufen. Die App berechnet den durch bereits getätigte Käufe erreichten Rabatt 
  und zeigt diesen auf den Karten an. 
- Inventory (InventoryFragment.kt und fragment_inventory.xml) zeigt eine Liste aller bereits gekauften Karten an.
- Library (AllCardsFragment.kt und fragment_all_cards.xml) zeigt eine Liste aller Karten im Spiel an.
- Tips (TipsFragment.kt und fragment_tips.xml) gibt eine Hilfestellung für jede Zivilisation.
- Wo notwendig, gibt es unterschiedliche Layouts für Day/Night bzw. hochkant und quer.

# Coding Style 
- Die Entwicklungssprache der App ist englisch. Kommentare im Code sind immer in englisch zu schreiben.
- Wenn du Änderungen am Code vornimmst, dann beschränke dich immer nur auf die gestellte Anfrage.
- Lösche keine Kommentare von mir.
- Optimierungen im Code sind gesondert auszuweisen als extra Änderung.
- falls Änderungen am Layout vorgenommen werden, auch überprüfen, ob es andere Layouts dieser Seite gibt und entsprechend für Day/Night hochkant/quer anpassen.
