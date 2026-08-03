# Klexikon-App für Android

Eine bewusst eingeschränkte WebView-App für Kinder. Sie öffnet ausschließlich
`https://klexikon.zum.de/` und blockiert fremde Navigationen, Downloads,
Pop-ups, Datei- und Inhaltszugriffe. Medien von `upload.wikimedia.org` sind
als reine Seitenressourcen erlaubt, damit Bilder aus Wikimedia Commons laden.

## Build

```bash
gradle :app:assembleRelease
```

Die APK entsteht unter `app/build/outputs/apk/release/app-release.apk`.
