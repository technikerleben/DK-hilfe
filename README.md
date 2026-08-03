# Dänemark Hilfe

Eine vollständig offline nutzbare Android-App für Android 8 bis 15. Sie besitzt bewusst keine Internetberechtigung.

## Enthalten

- DKK/EUR-Rechner mit manuell änderbarem Kurs
- dänische Kommunikationsphrasen mit Favoriten und eingebetteter Offline-Aussprache
- großformatige Allergie-Karte
- Notruf- und Polizeinummern
- kompakte Hinweise zum Straßenverkehr
- Temperatur-, Strecken- und Wind-Umrechner
- lokal gespeicherte Reise-Checkliste

## Bauen

Das Projekt in Android Studio öffnen, die Gradle-Synchronisierung abwarten und **Build > Build APK(s)** wählen. Die Debug-APK liegt anschließend unter `app/build/outputs/apk/debug/app-debug.apk`.

Die App lädt `app/src/main/assets/index.html` ausschließlich lokal. Im Manifest ist keine Internetberechtigung eingetragen.

Die 30 dänischen Sprachdateien liegen ebenfalls lokal unter `app/src/main/assets/audio`. Sie werden direkt aus dem APK-Paket abgespielt und benötigen weder eine installierte dänische Systemstimme noch eine Internetverbindung.
